package de.gyrosbande.dice.domain

/** One player's finished roll within a round. */
data class PlayerOutcome(
    val player: Player,
    val outcome: RollOutcome,
)

/**
 * Tracks whose turn it is during a round and collects the results.
 * Pure Kotlin, no Android dependencies - fully unit-testable.
 * The actual rolling is done by [GameFlow]; this class only owns the
 * player order and the collected outcomes.
 */
class RoundSession(val players: List<Player>) {

    init {
        require(players.isNotEmpty()) { "A round needs at least one player" }
    }

    private val recorded = mutableListOf<PlayerOutcome>()

    val results: List<PlayerOutcome> get() = recorded

    /** The player whose turn it is, or null when everyone has rolled. */
    val currentPlayer: Player? get() = players.getOrNull(recorded.size)

    val isFinished: Boolean get() = recorded.size == players.size

    /** Record the current player's outcome and advance to the next player. */
    fun record(outcome: RollOutcome) {
        val player = currentPlayer ?: error("Round is already finished")
        recorded += PlayerOutcome(player, outcome)
    }
}
