package de.gyrosbande.dice.data

import de.gyrosbande.dice.data.db.RollResultEntity
import de.gyrosbande.dice.data.db.RoundDao
import de.gyrosbande.dice.data.db.RoundEntity
import de.gyrosbande.dice.domain.PlayerOutcome

class RoundRepository(private val roundDao: RoundDao) {

    /** Starts a new round and returns its id. */
    suspend fun startRound(): Long =
        roundDao.insertRound(RoundEntity(startedAt = System.currentTimeMillis()))

    /** Persists one player's result as a snapshot (see [RollResultEntity]). */
    suspend fun saveResult(roundId: Long, result: PlayerOutcome, wasVirtual: Boolean) {
        val outcome = result.outcome
        roundDao.insertResult(
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
                wasVirtual = wasVirtual,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun finishRound(roundId: Long) =
        roundDao.finishRound(roundId, System.currentTimeMillis())
}
