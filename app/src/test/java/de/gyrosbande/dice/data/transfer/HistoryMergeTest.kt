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

    @Test
    fun `an empty import plans nothing`() {
        val plan = HistoryMerge.plan(
            import = export(players = emptyList(), rounds = emptyList()),
            existingRoundUuids = emptySet(),
            existingPlayerNames = emptyList(),
        )
        assertTrue(plan.roundsToImport.isEmpty())
        assertTrue(plan.playersToCreate.isEmpty())
        assertEquals(0, plan.report.importedRounds)
        assertEquals(0, plan.report.skippedRounds)
    }

    @Test
    fun `an import where every round is already known imports nothing`() {
        val plan = HistoryMerge.plan(
            import = export(
                players = listOf("Marcel"),
                rounds = listOf(exportRound("a", "Marcel"), exportRound("b", "Marcel")),
            ),
            existingRoundUuids = setOf("a", "b"),
            existingPlayerNames = listOf("Marcel"),
        )
        assertTrue(plan.roundsToImport.isEmpty())
        assertEquals(0, plan.report.importedRounds)
        assertEquals(2, plan.report.skippedRounds)
        assertTrue(plan.report.newPlayers.isEmpty())
    }

    @Test
    fun `blank player names are not proposed as new players`() {
        val plan = HistoryMerge.plan(
            import = export(
                players = listOf("   "),
                rounds = listOf(exportRound("a", "   ", "Marcel")),
            ),
            existingRoundUuids = emptySet(),
            existingPlayerNames = emptyList(),
        )
        assertEquals(listOf("Marcel"), plan.report.newPlayers)
    }

    @Test
    fun `a round with no results imports fine without adding players`() {
        val emptyRound = ExportRound(uuid = "a", startedAt = 1_000L, finishedAt = 2_000L, results = emptyList())
        val plan = HistoryMerge.plan(
            import = export(players = emptyList(), rounds = listOf(emptyRound)),
            existingRoundUuids = emptySet(),
            existingPlayerNames = emptyList(),
        )
        assertEquals(listOf("a"), plan.roundsToImport.map { it.uuid })
        assertTrue(plan.playersToCreate.isEmpty())
    }

    @Test
    fun `the players list can add a player even from an already-known round`() {
        // Documents the intentional "bonus" behavior: the top-level players
        // list is still consulted even when its round was already imported
        // before, e.g. a player who joined but never rolled in that round.
        val plan = HistoryMerge.plan(
            import = export(
                players = listOf("Marcel", "Jonas"),
                rounds = listOf(exportRound("a", "Marcel")),
            ),
            existingRoundUuids = setOf("a"),
            existingPlayerNames = listOf("Marcel"),
        )
        assertEquals(0, plan.report.importedRounds)
        assertEquals(listOf("Jonas"), plan.report.newPlayers)
    }

    @Test
    fun `existing player names are normalized before matching too`() {
        val plan = HistoryMerge.plan(
            import = export(players = emptyList(), rounds = listOf(exportRound("a", "MARCEL"))),
            existingRoundUuids = emptySet(),
            existingPlayerNames = listOf("  marcel  "),
        )
        assertTrue("Marcel should already be known despite case/whitespace differences", plan.report.newPlayers.isEmpty())
    }

    @Test
    fun `duplicate names in the players list are only added once`() {
        val plan = HistoryMerge.plan(
            import = export(players = listOf("Jonas", "Jonas", "jonas "), rounds = emptyList()),
            existingRoundUuids = emptySet(),
            existingPlayerNames = emptyList(),
        )
        assertEquals(listOf("Jonas"), plan.report.newPlayers)
    }

    @Test
    fun `many new rounds and players are all planned correctly`() {
        val rounds = (1..20).map { i -> exportRound("round-$i", "Player$i") }
        val plan = HistoryMerge.plan(
            import = export(players = emptyList(), rounds = rounds),
            existingRoundUuids = emptySet(),
            existingPlayerNames = emptyList(),
        )
        assertEquals(20, plan.report.importedRounds)
        assertEquals(20, plan.playersToCreate.size)
    }
}
