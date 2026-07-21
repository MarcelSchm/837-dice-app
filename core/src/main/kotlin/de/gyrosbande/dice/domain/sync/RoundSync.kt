package de.gyrosbande.dice.domain.sync

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Where a player stands within their turn (phase 2b - connected rounds). */
enum class RoundStage {
    /** Roll 1 is up: the category. */
    CATEGORY,

    /** Roll 2 is up: the drink within the rolled category. */
    DRINK,

    /** The drink is rolled; the result is shown, waiting to be confirmed. */
    RESULT,

    /** Every player has rolled - the round is done (order summary on the phone). */
    DONE,
}

/**
 * The live state of a phone round, mirrored to the watch as a passive second
 * display. The phone stays the source of truth and does all the rolling; the
 * watch only shows this state.
 */
@Serializable
data class WatchRoundState(
    val active: Boolean = false,
    val currentPlayer: String? = null,
    /** 0-based index of the player currently rolling. */
    val playerIndex: Int = 0,
    val totalPlayers: Int = 0,
    val stage: RoundStage = RoundStage.CATEGORY,
    val rolling: Boolean = false,
    /** Category name, shown from the drink roll onwards. */
    val category: String? = null,
    /** Result drink name and price, set while [stage] is RESULT. */
    val resultDrink: String? = null,
    val resultPrice: String? = null,
) {
    companion object {
        /** The single "no round in progress" state. */
        val INACTIVE = WatchRoundState(active = false)
    }
}

object RoundSync {

    /** Data Layer item path the phone writes the round state to. */
    const val PATH = "/round"

    /** Key inside the round data item holding the JSON payload. */
    const val KEY_JSON = "round_json"

    private val json = Json { ignoreUnknownKeys = true }

    fun encode(state: WatchRoundState): String = json.encodeToString(state)

    /** Parses a round state; returns [WatchRoundState.INACTIVE] if unreadable. */
    fun decode(text: String?): WatchRoundState {
        if (text.isNullOrBlank()) return WatchRoundState.INACTIVE
        return try {
            json.decodeFromString<WatchRoundState>(text)
        } catch (e: Exception) {
            WatchRoundState.INACTIVE
        }
    }
}
