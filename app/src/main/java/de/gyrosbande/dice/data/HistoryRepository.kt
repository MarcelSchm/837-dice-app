package de.gyrosbande.dice.data

import androidx.room.withTransaction
import de.gyrosbande.dice.data.db.AppDatabase
import de.gyrosbande.dice.data.db.PlayerEntity
import de.gyrosbande.dice.data.db.RollResultEntity
import de.gyrosbande.dice.data.db.RoundEntity
import de.gyrosbande.dice.data.db.RoundWithResults
import de.gyrosbande.dice.data.transfer.ExportPlayer
import de.gyrosbande.dice.data.transfer.ExportResult
import de.gyrosbande.dice.data.transfer.ExportRound
import de.gyrosbande.dice.data.transfer.HistoryExport
import de.gyrosbande.dice.data.transfer.HistoryMerge
import de.gyrosbande.dice.data.transfer.MergeReport
import de.gyrosbande.dice.domain.HistoryResult
import de.gyrosbande.dice.domain.HistoryRound
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** History reading plus export/import with cross-device merging. */
class HistoryRepository(private val database: AppDatabase) {

    private val roundDao get() = database.roundDao()
    private val playerDao get() = database.playerDao()

    fun observeHistory(): Flow<List<HistoryRound>> =
        roundDao.observeFinishedRounds().map { rows -> rows.map { it.toDomain() } }

    /**
     * Deletes a round (and its results) permanently. Note: importing an
     * older export file that still contains the round will bring it back -
     * that's inherent to the idempotent merge design.
     */
    suspend fun deleteRound(uuid: String) = roundDao.deleteRoundByUuid(uuid)

    suspend fun buildExport(appVersion: String): HistoryExport =
        HistoryExport(
            exportedAt = System.currentTimeMillis(),
            appVersion = appVersion,
            players = playerDao.players().map { ExportPlayer(it.name) },
            rounds = roundDao.finishedRounds().map { it.toExport() },
        )

    /**
     * Merges an export from another device into the local history.
     * Idempotent: rounds already present (by uuid) are skipped.
     */
    suspend fun import(export: HistoryExport): MergeReport = database.withTransaction {
        val plan = HistoryMerge.plan(
            import = export,
            existingRoundUuids = roundDao.allRoundUuids().toSet(),
            existingPlayerNames = playerDao.players().map { it.name },
        )

        plan.playersToCreate.forEach { name ->
            playerDao.insert(PlayerEntity(name = name))
        }
        // Player ids for the imported result rows (match by trimmed lowercase name).
        val playerIds = playerDao.players().associateBy(
            { it.name.trim().lowercase() },
            { it.id },
        )

        plan.roundsToImport.forEach { round ->
            val roundId = roundDao.insertRound(
                RoundEntity(
                    uuid = round.uuid,
                    startedAt = round.startedAt,
                    finishedAt = round.finishedAt,
                )
            )
            roundDao.insertResults(
                round.results.map { result ->
                    RollResultEntity(
                        roundId = roundId,
                        playerId = playerIds[result.playerName.trim().lowercase()] ?: 0,
                        playerName = result.playerName,
                        categoryName = result.categoryName,
                        drinkName = result.drinkName,
                        drinkSizeLabel = result.drinkSizeLabel,
                        priceCents = result.priceCents,
                        categoryRoll = result.categoryRoll,
                        drinkRolls = result.drinkRolls.joinToString(","),
                        categorySize = result.categorySize,
                        substituted = result.substituted,
                        wasVirtual = result.wasVirtual,
                        createdAt = result.createdAt,
                    )
                }
            )
        }

        plan.report
    }

    private fun RoundWithResults.toDomain() = HistoryRound(
        uuid = round.uuid,
        startedAt = round.startedAt,
        finishedAt = round.finishedAt ?: round.startedAt,
        results = results.sortedBy { it.createdAt }.map { result ->
            HistoryResult(
                playerName = result.playerName,
                categoryName = result.categoryName,
                drinkName = result.drinkName,
                sizeLabel = result.drinkSizeLabel,
                priceCents = result.priceCents,
                categoryRoll = result.categoryRoll,
                drinkRolls = result.drinkRolls.split(",").mapNotNull { it.toIntOrNull() },
                categorySize = result.categorySize,
                substituted = result.substituted,
                wasVirtual = result.wasVirtual,
            )
        },
    )

    private fun RoundWithResults.toExport() = ExportRound(
        uuid = round.uuid,
        startedAt = round.startedAt,
        finishedAt = round.finishedAt ?: round.startedAt,
        results = results.sortedBy { it.createdAt }.map { result ->
            ExportResult(
                playerName = result.playerName,
                categoryName = result.categoryName,
                drinkName = result.drinkName,
                drinkSizeLabel = result.drinkSizeLabel,
                priceCents = result.priceCents,
                categoryRoll = result.categoryRoll,
                drinkRolls = result.drinkRolls.split(",").mapNotNull { it.toIntOrNull() },
                categorySize = result.categorySize,
                substituted = result.substituted,
                wasVirtual = result.wasVirtual,
                createdAt = result.createdAt,
            )
        },
    )
}
