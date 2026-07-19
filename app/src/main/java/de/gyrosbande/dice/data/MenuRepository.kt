package de.gyrosbande.dice.data

import de.gyrosbande.dice.data.db.CategoryEntity
import de.gyrosbande.dice.data.db.DrinkEntity
import de.gyrosbande.dice.data.db.MenuDao
import de.gyrosbande.dice.domain.Category
import de.gyrosbande.dice.domain.Drink
import de.gyrosbande.dice.domain.MenuSeed

/**
 * Single source of truth for the drinks menu. Seeds the database with the
 * San Remo menu ([MenuSeed]) on first access; after that the database wins
 * (it will become editable in the app).
 */
class MenuRepository(private val menuDao: MenuDao) {

    suspend fun categories(): List<Category> {
        seedIfEmpty()
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

    private suspend fun seedIfEmpty() {
        if (menuDao.categoryCount() > 0) return
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
