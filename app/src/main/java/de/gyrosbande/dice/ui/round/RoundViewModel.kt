package de.gyrosbande.dice.ui.round

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gyrosbande.dice.data.MenuRepository
import de.gyrosbande.dice.data.PlayerRepository
import de.gyrosbande.dice.data.RoundRepository
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

    /** Manually added order lines (food, beer ...) with their row ids. */
    var extras by mutableStateOf<List<Pair<Long, ExtraItem>>>(emptyList())
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
            roundRepository.saveResult(id, results.last(), wasVirtual)
            if (activeSession.isFinished) {
                roundRepository.finishRound(id)
            } else {
                activeController.reset()
            }
        }
    }
}
