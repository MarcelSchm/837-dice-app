package de.gyrosbande.dice.ui.round

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gyrosbande.dice.data.MenuRepository
import de.gyrosbande.dice.data.PlayerRepository
import de.gyrosbande.dice.data.RoundRepository
import de.gyrosbande.dice.domain.Drink
import de.gyrosbande.dice.domain.ExtraItem
import de.gyrosbande.dice.domain.Player
import de.gyrosbande.dice.domain.PlayerOutcome
import de.gyrosbande.dice.domain.RollPhase
import de.gyrosbande.dice.domain.RoundSession
import de.gyrosbande.dice.ui.roll.RollController
import de.gyrosbande.dice.ui.roll.RollMode
import kotlinx.coroutines.launch

/**
 * One round: all active players roll in turn, every result is persisted,
 * and the round ends in the grouped order summary.
 */
class RoundViewModel(
    private val menuRepository: MenuRepository,
    private val playerRepository: PlayerRepository,
    private val roundRepository: RoundRepository,
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set

    /** Null when there are no active players (or still loading). */
    var controller by mutableStateOf<RollController?>(null)
        private set

    private var session: RoundSession? = null
    private var roundId: Long? = null

    /** Mirror of the session results as Compose state. */
    var results by mutableStateOf<List<PlayerOutcome>>(emptyList())
        private set

    /**
     * Row ids of the saved results, parallel to [results] - needed to
     * rewrite a result when San Remo doesn't have the drink.
     */
    private val resultIds = mutableListOf<Long>()

    /** Manually added order lines (food, beer ...) with their row ids. */
    var extras by mutableStateOf<List<Pair<Long, ExtraItem>>>(emptyList())
        private set

    /**
     * Drinks San Remo told us they are out of (by name, for this round).
     * Everyone who rolled one has to roll again, and rolling it again
     * warns instead of being accepted.
     */
    var unavailableDrinks by mutableStateOf<Set<String>>(emptySet())
        private set

    /** Result indices still waiting to be re-rolled, in turn order. */
    var redoQueue by mutableStateOf<List<Int>>(emptyList())
        private set

    /** Whose result is being re-rolled right now, or null. */
    val redoIndex: Int? get() = redoQueue.firstOrNull()

    /** Position of the current re-roll, e.g. "2 von 3". */
    val redoDone: Int get() = redoTotal - redoQueue.size + 1
    var redoTotal by mutableStateOf(0)
        private set

    val players: List<Player> get() = session?.players ?: emptyList()
    val currentPlayer: Player? get() = session?.players?.getOrNull(results.size)
    val isFinished: Boolean get() = session != null && results.size == players.size

    init {
        viewModelScope.launch {
            val activePlayers = playerRepository.activePlayers()
            if (activePlayers.isNotEmpty()) {
                session = RoundSession(activePlayers)
                controller = RollController(menuRepository.categories())
            }
            loading = false
        }
    }

    fun rollVirtual() {
        viewModelScope.launch { controller?.rollVirtual() }
    }

    /** True when San Remo said they are out of this drink. */
    fun isUnavailable(drink: Drink): Boolean = drink.name in unavailableDrinks

    /** Names of the players who rolled [drink], in turn order. */
    fun playersWith(drink: Drink): List<String> =
        results.filter { it.outcome.drink.name == drink.name }.map { it.player.name }

    /** Add a manual order line (food, beer ...) to the round. */
    fun addExtra(extra: ExtraItem) {
        viewModelScope.launch {
            val id = roundId ?: roundRepository.startRound().also { roundId = it }
            val rowId = roundRepository.addExtra(id, extra)
            extras = extras + (rowId to extra)
        }
    }

    fun removeExtra(index: Int) {
        val (rowId, _) = extras.getOrNull(index) ?: return
        viewModelScope.launch {
            roundRepository.removeExtra(rowId)
            extras = extras.filterIndexed { i, _ -> i != index }
        }
    }

    /** Save the current player's finished roll and advance to the next one. */
    fun confirmResult() {
        val activeController = controller ?: return
        val activeSession = session ?: return
        val finished = activeController.state.phase as? RollPhase.Finished ?: return
        val wasVirtual = activeController.state.mode == RollMode.VIRTUAL

        viewModelScope.launch {
            val id = roundId ?: roundRepository.startRound().also { roundId = it }
            activeSession.record(finished.outcome)
            results = activeSession.results.toList()
            resultIds += roundRepository.saveResult(id, results.last(), wasVirtual)
            if (activeSession.isFinished) {
                roundRepository.finishRound(id)
            } else {
                activeController.reset()
            }
        }
    }

    // --- "They don't have that" while reading out the order ------------

    /**
     * San Remo is out of [drink]: remember it for this round and queue up
     * everyone who rolled it, so they re-roll one after another.
     */
    fun markDrinkUnavailable(drink: Drink) {
        val affected = results.indices.filter { results[it].outcome.drink.name == drink.name }
        if (affected.isEmpty()) return
        unavailableDrinks = unavailableDrinks + drink.name
        redoQueue = affected
        redoTotal = affected.size
        startRedoFor(affected.first())
    }

    /** Aborts the whole re-roll queue; the drink stays marked. */
    fun cancelRedo() {
        redoQueue = emptyList()
        redoTotal = 0
    }

    /** Take over the re-rolled drink and move on to the next player. */
    fun confirmRedo() {
        val index = redoIndex ?: return
        val activeController = controller ?: return
        val finished = activeController.state.phase as? RollPhase.Finished ?: return
        // Never accept a drink they already told us is out.
        if (isUnavailable(finished.outcome.drink)) return
        val wasVirtual = activeController.state.mode == RollMode.VIRTUAL

        viewModelScope.launch {
            val updated = PlayerOutcome(results[index].player, finished.outcome)
            resultIds.getOrNull(index)?.let {
                roundRepository.updateResult(it, updated, wasVirtual)
            }
            results = results.toMutableList().also { it[index] = updated }

            val remaining = redoQueue.drop(1)
            redoQueue = remaining
            if (remaining.isEmpty()) redoTotal = 0 else startRedoFor(remaining.first())
        }
    }

    private fun startRedoFor(index: Int) {
        val outcome = results.getOrNull(index)?.outcome ?: return
        controller?.redoDrinkRoll(outcome.category, outcome.categoryRoll)
    }
}
