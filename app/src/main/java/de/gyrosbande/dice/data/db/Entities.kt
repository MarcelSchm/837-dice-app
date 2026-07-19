package de.gyrosbande.dice.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Pip count of the category roll (1-6). */
    val diceNumber: Int,
    val sortOrder: Int,
)

@Entity(
    tableName = "drinks",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("categoryId")],
)
data class DrinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val name: String,
    val priceCents: Int,
    val sizeLabel: String?,
    /** Position within the category - game-relevant (wrap rule). */
    val sortOrder: Int,
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Only active players take part in a round. */
    val isActive: Boolean = true,
)

@Entity(tableName = "rounds")
data class RoundEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startedAt: Long,
    val finishedAt: Long? = null,
)

/**
 * One player's result within a round. Drink data is stored as a snapshot
 * (name/price at the time of rolling) so later menu edits don't rewrite
 * history.
 */
@Entity(
    tableName = "roll_results",
    foreignKeys = [
        ForeignKey(
            entity = RoundEntity::class,
            parentColumns = ["id"],
            childColumns = ["roundId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("roundId")],
)
data class RollResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roundId: Long,
    val playerId: Long,
    val playerName: String,
    val categoryName: String,
    val drinkName: String,
    val drinkSizeLabel: String?,
    val priceCents: Int,
    val categoryRoll: Int,
    /** Pip values of the drink roll, comma-separated (e.g. "4,5"). */
    val drinkRolls: String,
    val wasVirtual: Boolean,
    val createdAt: Long,
)
