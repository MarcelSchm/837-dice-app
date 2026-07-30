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

    private fun correctedRound(uuid: String, updatedAt: Long, vararg players: String) =
        exportRound(uuid, *players).copy(updatedAt = updatedAt)

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
            existingRounds = mapOf("a" to null),
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
        val first = HistoryMerge.plan(file, emptyMap(), emptyList())
        assertEquals(1, first.report.importedRounds)

        // After the first import the round uuid and player exist locally.
        val second = HistoryMerge.plan(file, mapOf("a" to null), listOf("Marcel"))
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
            existingRounds = emptyMap(),
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
            existingRounds = emptyMap(),
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
            existingRounds = emptyMap(),
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
            existingRounds = mapOf("a" to null, "b" to null),
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
            existingRounds = emptyMap(),
            existingPlayerNames = emptyList(),
        )
        assertEquals(listOf("Marcel"), plan.report.newPlayers)
    }

    @Test
    fun `a round with no results imports fine without adding players`() {
        val emptyRound = ExportRound(uuid = "a", startedAt = 1_000L, finishedAt = 2_000L, results = emptyList())
        val plan = HistoryMerge.plan(
            import = export(players = emptyList(), rounds = listOf(emptyRound)),
            existingRounds = emptyMap(),
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
            existingRounds = mapOf("a" to null),
            existingPlayerNames = listOf("Marcel"),
        )
        assertEquals(0, plan.report.importedRounds)
        assertEquals(listOf("Jonas"), plan.report.newPlayers)
    }

    @Test
    fun `existing player names are normalized before matching too`() {
        val plan = HistoryMerge.plan(
            import = export(players = emptyList(), rounds = listOf(exportRound("a", "MARCEL"))),
            existingRounds = emptyMap(),
            existingPlayerNames = listOf("  marcel  "),
        )
        assertTrue("Marcel should already be known despite case/whitespace differences", plan.report.newPlayers.isEmpty())
    }

    @Test
    fun `duplicate names in the players list are only added once`() {
        val plan = HistoryMerge.plan(
            import = export(players = listOf("Jonas", "Jonas", "jonas "), rounds = emptyList()),
            existingRounds = emptyMap(),
            existingPlayerNames = emptyList(),
        )
        assertEquals(listOf("Jonas"), plan.report.newPlayers)
    }

    // --- Corrections travelling between phones (updatedAt) --------------

    @Test
    fun `a round corrected on the other phone replaces the local one`() {
        // Marcel adds Steffi to a round Kevin already has. Without this,
        // Kevin would keep the old three-player version forever.
        val plan = HistoryMerge.plan(
            import = export(
                players = emptyList(),
                rounds = listOf(correctedRound("a", updatedAt = 5_000L, "Marcel", "Steffi")),
            ),
            existingRounds = mapOf("a" to 2_000L),
            existingPlayerNames = listOf("Marcel"),
        )
        assertEquals(listOf("a"), plan.roundsToReplace.map { it.uuid })
        assertTrue(plan.roundsToImport.isEmpty())
        assertEquals(1, plan.report.updatedRounds)
        assertEquals(0, plan.report.skippedRounds)
        // The correction brings the player it is all about.
        assertEquals(listOf("Steffi"), plan.report.newPlayers)
    }

    @Test
    fun `an older incoming round never overwrites a newer local one`() {
        val plan = HistoryMerge.plan(
            import = export(
                players = emptyList(),
                rounds = listOf(correctedRound("a", updatedAt = 1_000L, "Marcel")),
            ),
            existingRounds = mapOf("a" to 9_000L),
            existingPlayerNames = listOf("Marcel"),
        )
        assertTrue(plan.roundsToReplace.isEmpty())
        assertEquals(1, plan.report.skippedRounds)
    }

    @Test
    fun `re-importing the same corrected file stays a no-op`() {
        // Same timestamp means same version - idempotence must survive.
        val file = export(
            players = emptyList(),
            rounds = listOf(correctedRound("a", updatedAt = 5_000L, "Marcel")),
        )
        val plan = HistoryMerge.plan(file, mapOf("a" to 5_000L), listOf("Marcel"))
        assertTrue(plan.roundsToReplace.isEmpty())
        assertEquals(0, plan.report.updatedRounds)
        assertEquals(1, plan.report.skippedRounds)
    }

    @Test
    fun `a corrected round beats a local one that was never touched`() {
        // Local rounds from before schema v5 have no timestamp at all.
        val plan = HistoryMerge.plan(
            import = export(
                players = emptyList(),
                rounds = listOf(correctedRound("a", updatedAt = 5_000L, "Marcel")),
            ),
            existingRounds = mapOf("a" to null),
            existingPlayerNames = listOf("Marcel"),
        )
        assertEquals(1, plan.report.updatedRounds)
    }

    @Test
    fun `a file from an older app version never overwrites anything`() {
        // No updatedAt in the file - we cannot tell, so we keep what we have.
        val plan = HistoryMerge.plan(
            import = export(players = emptyList(), rounds = listOf(exportRound("a", "Marcel"))),
            existingRounds = mapOf("a" to 5_000L),
            existingPlayerNames = listOf("Marcel"),
        )
        assertTrue(plan.roundsToReplace.isEmpty())
        assertEquals(1, plan.report.skippedRounds)
    }

    @Test
    fun `many new rounds and players are all planned correctly`() {
        val rounds = (1..20).map { i -> exportRound("round-$i", "Player$i") }
        val plan = HistoryMerge.plan(
            import = export(players = emptyList(), rounds = rounds),
            existingRounds = emptyMap(),
            existingPlayerNames = emptyList(),
        )
        assertEquals(20, plan.report.importedRounds)
        assertEquals(20, plan.playersToCreate.size)
    }
}
