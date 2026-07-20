package de.gyrosbande.dice.data

import androidx.room.withTransaction
import de.gyrosbande.dice.data.db.AppDatabase
import de.gyrosbande.dice.data.db.CategoryEntity
import de.gyrosbande.dice.data.db.DrinkEntity
import de.gyrosbande.dice.domain.Category
import de.gyrosbande.dice.domain.Drink
import de.gyrosbande.dice.domain.MenuSeed
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** A drink as shown in the menu editor (with its database id). */
data class EditableDrink(
    val id: Long,
    val name: String,
    val priceCents: Int,
    val sizeLabel: String?,
) {
    val priceFormatted: String
        get() = "%d,%02d €".format(priceCents / 100, priceCents % 100)
}

/** A category as shown in the menu editor. */
data class EditableCategory(
    val id: Long,
    val diceNumber: Int,
    val name: String,
    val drinks: List<EditableDrink>,
)

/**
 * Single source of truth for the drinks menu. Seeds the database with the
 * San Remo menu ([MenuSeed]) on first access; after that the database wins -
 * the menu is editable in the app (prices and lineup change over the years).
 */
class MenuRepository(private val database: AppDatabase) {

    private val menuDao get() = database.menuDao()

    suspend fun categories(): List<Category> {
        ensureSeeded()
        return menuDao.categoriesWithDrinks().map { row ->
            Category(
                diceNumber = row.category.diceNumber,
                name = row.category.name,
                drinks = row.drinks
                    .sortedBy { it.sortOrder }
                    .map { Drink(it.name, it.priceCents, it.sizeLabel) },
            )
        }
    }

    fun observeEditableMenu(): Flow<List<EditableCategory>> =
        menuDao.observeCategoriesWithDrinks().map { rows ->
            rows.map { row ->
                EditableCategory(
                    id = row.category.id,
                    diceNumber = row.category.diceNumber,
                    name = row.category.name,
                    drinks = row.drinks
                        .sortedBy { it.sortOrder }
                        .map { EditableDrink(it.id, it.name, it.priceCents, it.sizeLabel) },
                )
            }
        }

    suspend fun ensureSeeded() {
        if (menuDao.categoryCount() > 0) return
        insertSeed()
    }

    /** Throws away all edits and restores the original San Remo menu. */
    suspend fun resetToSeed() = database.withTransaction {
        menuDao.deleteAllCategories()
        insertSeed()
    }

    suspend fun renameCategory(categoryId: Long, name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val category = menuDao.categoryById(categoryId) ?: return
        menuDao.updateCategory(category.copy(name = trimmed))
    }

    /**
     * Assigns [newNumber] to the category; whichever category held that
     * pip count before takes over the old number (a swap, so every number
     * 1-6 always maps to exactly one category).
     */
    suspend fun swapDiceNumber(categoryId: Long, newNumber: Int) = database.withTransaction {
        val category = menuDao.categoryById(categoryId) ?: return@withTransaction
        if (category.diceNumber == newNumber) return@withTransaction
        val other = menuDao.categoriesWithDrinks()
            .map { it.category }
            .firstOrNull { it.diceNumber == newNumber }
        other?.let { menuDao.updateCategory(it.copy(diceNumber = category.diceNumber)) }
        menuDao.updateCategory(category.copy(diceNumber = newNumber))
    }

    suspend fun addDrink(categoryId: Long, name: String, priceCents: Int, sizeLabel: String?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val nextOrder = (menuDao.drinksOf(categoryId).maxOfOrNull { it.sortOrder } ?: -1) + 1
        menuDao.insertDrink(
            DrinkEntity(
                categoryId = categoryId,
                name = trimmed,
                priceCents = priceCents,
                sizeLabel = sizeLabel?.trim()?.takeIf { it.isNotEmpty() },
                sortOrder = nextOrder,
            )
        )
    }

    suspend fun updateDrink(drinkId: Long, name: String, priceCents: Int, sizeLabel: String?) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val drink = menuDao.drinkById(drinkId) ?: return
        menuDao.updateDrink(
            drink.copy(
                name = trimmed,
                priceCents = priceCents,
                sizeLabel = sizeLabel?.trim()?.takeIf { it.isNotEmpty() },
            )
        )
    }

    suspend fun deleteDrink(drinkId: Long) = menuDao.deleteDrinkById(drinkId)

    /**
     * Moves a drink one position up or down within its category by swapping
     * sort orders with its neighbor. Order is game-relevant (wrap rule)!
     */
    suspend fun moveDrink(drinkId: Long, up: Boolean) = database.withTransaction {
        val drink = menuDao.drinkById(drinkId) ?: return@withTransaction
        val siblings = menuDao.drinksOf(drink.categoryId)
        val index = siblings.indexOfFirst { it.id == drinkId }
        val neighborIndex = if (up) index - 1 else index + 1
        val neighbor = siblings.getOrNull(neighborIndex) ?: return@withTransaction
        menuDao.updateDrink(drink.copy(sortOrder = neighbor.sortOrder))
        menuDao.updateDrink(neighbor.copy(sortOrder = drink.sortOrder))
    }

    private suspend fun insertSeed() {
        MenuSeed.categories.forEachIndexed { categoryIndex, category ->
            val categoryId = menuDao.insertCategory(
                CategoryEntity(
                    name = category.name,
                    diceNumber = category.diceNumber,
                    sortOrder = categoryIndex,
                )
            )
            menuDao.insertDrinks(
                category.drinks.mapIndexed { drinkIndex, drink ->
                    DrinkEntity(
                        categoryId = categoryId,
                        name = drink.name,
                        priceCents = drink.priceCents,
                        sizeLabel = drink.sizeLabel,
                        sortOrder = drinkIndex,
                    )
                }
            )
        }
    }
}
