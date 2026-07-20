package de.gyrosbande.dice.data

import de.gyrosbande.dice.data.db.ExtraOrderItemEntity
import de.gyrosbande.dice.data.db.RollResultEntity
import de.gyrosbande.dice.data.db.RoundDao
import de.gyrosbande.dice.data.db.RoundEntity
import de.gyrosbande.dice.domain.ExtraItem
import de.gyrosbande.dice.domain.PlayerOutcome
import java.util.UUID

class RoundRepository(private val roundDao: RoundDao) {

    /** Starts a new round and returns its id. */
    suspend fun startRound(): Long =
        roundDao.insertRound(
            RoundEntity(
                uuid = UUID.randomUUID().toString(),
                startedAt = System.currentTimeMillis(),
            )
        )

    /**
     * Persists one player's result as a snapshot (see [RollResultEntity])
     * and returns the row id, so the result can be corrected later.
     */
    suspend fun saveResult(roundId: Long, result: PlayerOutcome, wasVirtual: Boolean): Long {
        val outcome = result.outcome
        return roundDao.insertResult(
            RollResultEntity(
                roundId = roundId,
                playerId = result.player.id,
                playerName = result.player.name,
                categoryName = outcome.category.name,
                drinkName = outcome.drink.name,
                drinkSizeLabel = outcome.drink.sizeLabel,
                priceCents = outcome.drink.priceCents,
                categoryRoll = outcome.categoryRoll,
                drinkRolls = outcome.drinkRolls.joinToString(","),
                categorySize = outcome.category.drinks.size,
                substituted = outcome.substituted,
                wasVirtual = wasVirtual,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    /**
     * Rewrites a recorded result ("they don't have that" while ordering).
     * Keeps the row, the player and the original position in the round.
     */
    suspend fun updateResult(resultId: Long, result: PlayerOutcome, wasVirtual: Boolean) {
        val outcome = result.outcome
        roundDao.updateResult(
            id = resultId,
            categoryName = outcome.category.name,
            drinkName = outcome.drink.name,
            sizeLabel = outcome.drink.sizeLabel,
            priceCents = outcome.drink.priceCents,
            categoryRoll = outcome.categoryRoll,
            drinkRolls = outcome.drinkRolls.joinToString(","),
            categorySize = outcome.category.drinks.size,
            substituted = outcome.substituted,
            wasVirtual = wasVirtual,
        )
    }

    suspend fun finishRound(roundId: Long) =
        roundDao.finishRound(roundId, System.currentTimeMillis())

    /** Adds a manual order line (food, beer ...) and returns its row id. */
    suspend fun addExtra(roundId: Long, extra: ExtraItem): Long =
        roundDao.insertExtra(
            ExtraOrderItemEntity(
                roundId = roundId,
                label = extra.label,
                priceCents = extra.priceCents,
                quantity = extra.quantity,
                createdAt = System.currentTimeMillis(),
            )
        )

    suspend fun removeExtra(extraId: Long) = roundDao.deleteExtraById(extraId)
}
