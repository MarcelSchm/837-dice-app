package de.gyrosbande.dice.data.transfer

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * File format for sharing the round history between devices (e.g. via the
 * WhatsApp group). Versioned so future app versions can stay compatible.
 */
@Serializable
data class HistoryExport(
    val formatVersion: Int = FORMAT_VERSION,
    val exportedAt: Long,
    val appVersion: String,
    val players: List<ExportPlayer>,
    val rounds: List<ExportRound>,
) {
    companion object {
        const val FORMAT_VERSION = 1

        private val json = Json {
            ignoreUnknownKeys = true // stay lenient towards newer exports
            encodeDefaults = true
            prettyPrint = true
        }

        fun toJson(export: HistoryExport): String = json.encodeToString(export)

        /** Throws [kotlinx.serialization.SerializationException] on bad input. */
        fun fromJson(text: String): HistoryExport = json.decodeFromString(text)
    }
}

@Serializable
data class ExportPlayer(
    val name: String,
)

@Serializable
data class ExportRound(
    val uuid: String,
    val startedAt: Long,
    val finishedAt: Long,
    val results: List<ExportResult>,
)

@Serializable
data class ExportResult(
    val playerName: String,
    val categoryName: String,
    val drinkName: String,
    val drinkSizeLabel: String? = null,
    val priceCents: Int,
    val categoryRoll: Int,
    val drinkRolls: List<Int>,
    val categorySize: Int = 0,
    val substituted: Boolean = false,
    val wasVirtual: Boolean,
    val createdAt: Long,
)
