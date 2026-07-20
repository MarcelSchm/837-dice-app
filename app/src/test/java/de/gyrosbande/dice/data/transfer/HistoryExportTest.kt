package de.gyrosbande.dice.data.transfer

import kotlinx.serialization.SerializationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The export/import file format is user-facing (shared via WhatsApp, saved
 * to disk, picked from any file manager) so it needs to cope with anything
 * a user might feed it - not just well-formed exports from this app.
 */
class HistoryExportTest {

    private fun sampleResult() = ExportResult(
        playerName = "Marcel",
        categoryName = "Bitter",
        drinkName = "Jägermeister 35 %",
        drinkSizeLabel = "2 cl",
        priceCents = 250,
        categoryRoll = 3,
        drinkRolls = listOf(2),
        categorySize = 4,
        substituted = false,
        wasVirtual = true,
        createdAt = 1_000L,
    )

    private fun sampleExport() = HistoryExport(
        exportedAt = 5_000L,
        appVersion = "1.4",
        players = listOf(ExportPlayer("Marcel"), ExportPlayer("Kevin")),
        rounds = listOf(
            ExportRound(
                uuid = "round-1",
                startedAt = 1_000L,
                finishedAt = 2_000L,
                results = listOf(sampleResult()),
            )
        ),
    )

    // --- Successful roundtrips ---------------------------------------

    @Test
    fun `roundtrip preserves an export with rounds and players`() {
        val original = sampleExport()
        val decoded = HistoryExport.fromJson(HistoryExport.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `roundtrip preserves an empty export`() {
        val original = HistoryExport(
            exportedAt = 1L,
            appVersion = "1.4",
            players = emptyList(),
            rounds = emptyList(),
        )
        val decoded = HistoryExport.fromJson(HistoryExport.toJson(original))
        assertEquals(original, decoded)
    }

    @Test
    fun `roundtrip preserves unicode and emoji player names`() {
        val original = sampleExport().copy(
            players = listOf(ExportPlayer("Jörg 🍺"), ExportPlayer("Ünlü Çelik")),
        )
        val decoded = HistoryExport.fromJson(HistoryExport.toJson(original))
        assertEquals(original.players, decoded.players)
    }

    @Test
    fun `roundtrip preserves a round with several players and a substitution`() {
        val substituted = sampleResult().copy(drinkName = "Grog", substituted = true)
        val original = sampleExport().copy(
            rounds = listOf(
                ExportRound(
                    uuid = "round-x",
                    startedAt = 10L,
                    finishedAt = 20L,
                    results = listOf(sampleResult(), substituted),
                )
            ),
        )
        val decoded = HistoryExport.fromJson(HistoryExport.toJson(original))
        assertEquals(original, decoded)
        assertTrue(decoded.rounds.single().results[1].substituted)
    }

    // --- Invalid input --------------------------------------------------

    @Test
    fun `empty input is rejected`() {
        assertThrows(SerializationException::class.java) { HistoryExport.fromJson("") }
    }

    @Test
    fun `blank whitespace input is rejected`() {
        assertThrows(SerializationException::class.java) { HistoryExport.fromJson("   \n  ") }
    }

    @Test
    fun `plain text that is not json is rejected`() {
        assertThrows(SerializationException::class.java) {
            HistoryExport.fromJson("this is definitely not a json file")
        }
    }

    @Test
    fun `unrelated json from a different app is rejected`() {
        assertThrows(SerializationException::class.java) {
            HistoryExport.fromJson("""{"albumTitle": "Vacation Photos", "count": 42}""")
        }
    }

    @Test
    fun `a json array instead of an object is rejected`() {
        assertThrows(SerializationException::class.java) {
            HistoryExport.fromJson("[1, 2, 3]")
        }
    }

    @Test
    fun `truncated cut-off json is rejected`() {
        val truncated = HistoryExport.toJson(sampleExport()).dropLast(30)
        assertThrows(SerializationException::class.java) { HistoryExport.fromJson(truncated) }
    }

    @Test
    fun `wrong field type is rejected`() {
        assertThrows(SerializationException::class.java) {
            HistoryExport.fromJson(
                """{"exportedAt": "not-a-number", "appVersion": "1.4", "players": [], "rounds": []}"""
            )
        }
    }

    @Test
    fun `rounds as a string instead of a list is rejected`() {
        assertThrows(SerializationException::class.java) {
            HistoryExport.fromJson(
                """{"exportedAt": 1, "appVersion": "1.4", "players": [], "rounds": "oops"}"""
            )
        }
    }

    @Test
    fun `missing required fields are rejected`() {
        assertThrows(SerializationException::class.java) {
            // no "rounds" key at all
            HistoryExport.fromJson("""{"exportedAt": 1, "appVersion": "1.4", "players": []}""")
        }
    }

    @Test
    fun `an empty json object is rejected`() {
        assertThrows(SerializationException::class.java) { HistoryExport.fromJson("{}") }
    }

    // --- Forward/backward compatibility ---------------------------------

    @Test
    fun `unknown extra fields from a newer app version are ignored`() {
        val json = """
            {
              "formatVersion": 1,
              "exportedAt": 1000,
              "appVersion": "9.9-future",
              "players": [{"name": "Marcel", "favoriteEmoji": "🥃"}],
              "rounds": [],
              "someBrandNewTopLevelField": {"nested": true}
            }
        """.trimIndent()
        val decoded = HistoryExport.fromJson(json)
        assertEquals(listOf(ExportPlayer("Marcel")), decoded.players)
    }

    @Test
    fun `missing optional result fields fall back to their defaults`() {
        // Simulates a file written by an older app version that didn't
        // have categorySize/substituted/drinkSizeLabel yet.
        val json = """
            {
              "exportedAt": 1000, "appVersion": "1.1", "players": [],
              "rounds": [{
                "uuid": "round-1", "startedAt": 1000, "finishedAt": 2000,
                "results": [{
                  "playerName": "Marcel", "categoryName": "Bitter",
                  "drinkName": "Jägermeister 35 %", "priceCents": 250,
                  "categoryRoll": 3, "drinkRolls": [2],
                  "wasVirtual": true, "createdAt": 1000
                }]
              }]
            }
        """.trimIndent()
        val result = HistoryExport.fromJson(json).rounds.single().results.single()
        assertEquals(0, result.categorySize)
        assertEquals(false, result.substituted)
        assertNull(result.drinkSizeLabel)
    }

    @Test
    fun `missing formatVersion defaults to the current format`() {
        val json = """{"exportedAt": 1, "appVersion": "1.4", "players": [], "rounds": []}"""
        assertEquals(HistoryExport.FORMAT_VERSION, HistoryExport.fromJson(json).formatVersion)
    }

    @Test
    fun `a future formatVersion still decodes as long as the shape matches`() {
        val json = """{"formatVersion": 99, "exportedAt": 1, "appVersion": "9.9", "players": [], "rounds": []}"""
        assertEquals(99, HistoryExport.fromJson(json).formatVersion)
    }
}
