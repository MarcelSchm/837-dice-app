package de.gyrosbande.dice.data.transfer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryMergeTest {

    private fun exportResult(player: String) = ExportResult(
        playerName = player,
        categoryName = "Bitter",
        drinkName = "Jägermeister 35 %",
        drinkSizeLabel = "2 cl",
        priceCents = 250,
        categoryRoll = 3,
        drinkRolls = listOf(2),
        categorySize = 4,
        wasVirtual = true,
        createdAt = 1_000L,
    )

    private fun exportRound(uuid: String, vararg players: String) = ExportRound(
        uuid = uuid,
        startedAt = 1_000L,
        finishedAt = 2_000L,
        results = players.map { exportResult(it) },
    )

    private fun export(players: List<String>, rounds: List<ExportRound>) = HistoryExport(
        exportedAt = 3_000L,
        appVersion = "test",
        players = players.map { ExportPlayer(it) },
        rounds = rounds,
    )

    @Test
    fun `new rounds are imported and known uuids are skipped`() {
        val plan = HistoryMerge.plan(
            import = export(
                players = listOf("Marcel", "Kevin"),
                rounds = listOf(exportRound("a", "Marcel"), exportRound("b", "Kevin")),
            ),
            existingRoundUuids = setOf("a"),
            existingPlayerNames = listOf("Marcel", "Kevin"),
        )
        assertEquals(listOf("b"), plan.roundsToImport.map { it.uuid })
        assertEquals(1, plan.report.importedRounds)
        assertEquals(1, plan.report.skippedRounds)
        assertTrue(plan.report.newPlayers.isEmpty())
    }

    @Test
    fun `importing the same file twice is a no-op`() {
        val file = export(
            players = listOf("Marcel"),
            rounds = listOf(exportRound("a", "Marcel")),
        )
        val first = HistoryMerge.plan(file, emptySet(), emptyList())
        assertEquals(1, first.report.importedRounds)

        // After the first import the round uuid and player exist locally.
        val second = HistoryMerge.plan(file, setOf("a"), listOf("Marcel"))
        assertEquals(0, second.report.importedRounds)
        assertEquals(1, second.report.skippedRounds)
        assertTrue(second.report.newPlayers.isEmpty())
    }

    @Test
    fun `players are matched case-insensitively and trimmed`() {
        val plan = HistoryMerge.plan(
            import = export(
                players = listOf("marcel ", "Jonas"),
                rounds = listOf(exportRound("a", "MARCEL", "Jonas")),
            ),
            existingRoundUuids = emptySet(),
            existingPlayerNames = listOf("Marcel"),
        )
        // Marcel exists (case-insensitive); only Jonas is new, exactly once.
        assertEquals(listOf("Jonas"), plan.report.newPlayers)
    }

    @Test
    fun `duplicate uuids inside one file count once`() {
        val plan = HistoryMerge.plan(
            import = export(
                players = emptyList(),
                rounds = listOf(exportRound("a", "Marcel"), exportRound("a", "Marcel")),
            ),
            existingRoundUuids = emptySet(),
            existingPlayerNames = emptyList(),
        )
        assertEquals(1, plan.report.importedRounds)
    }

    @Test
    fun `json roundtrip preserves the export`() {
        val original = export(
            players = listOf("Marcel", "Kevin"),
            rounds = listOf(exportRound("a", "Marcel", "Kevin")),
        )
        val decoded = HistoryExport.fromJson(HistoryExport.toJson(original))
        assertEquals(original, decoded)
    }
}
