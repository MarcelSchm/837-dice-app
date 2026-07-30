package de.gyrosbande.dice.ui.lineup

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gyrosbande.dice.data.PlayerRepository
import de.gyrosbande.dice.data.RoundRepository
import de.gyrosbande.dice.domain.Player
import kotlinx.coroutines.launch

/**
 * Tonight's line-up: who sits at the table, and in which order. The player
 * list is the same every time, the seating is not - people skip a day, others
 * only turn up on Saturday.
 */
class LineupViewModel(
    private val playerRepository: PlayerRepository,
    private val roundRepository: RoundRepository,
) : ViewModel() {

    var loading by mutableStateOf(true)
        private set

    /** Everyone the app knows. */
    var allPlayers by mutableStateOf<List<Player>>(emptyList())
        private set

    /** Who plays tonight, in turn order. */
    var lineup by mutableStateOf<List<Player>>(emptyList())
        private set

    /** Known players who are not at the table (yet). */
    val bench: List<Player>
        get() = allPlayers.filterNot { player -> lineup.any { it.id == player.id } }

    init {
        viewModelScope.launch {
            val players = playerRepository.allPlayers()
            allPlayers = players
            lineup = suggestLineup(players)
            loading = false
        }
    }

    /**
     * Pre-fills the line-up with the players of the last round in their
     * order - at the festival roughly the same crowd sits down the same way.
     * Falls back to everyone ticked "spielt mit".
     */
    private suspend fun suggestLineup(players: List<Player>): List<Player> {
        val byName = players.associateBy { it.name.trim().lowercase() }
        val fromLastRound = roundRepository.lastRoundPlayerNames()
            .mapNotNull { byName[it.trim().lowercase()] }
            .distinctBy { it.id }
        return fromLastRound.ifEmpty { players.filter { it.isActive } }
    }

    fun addToLineup(player: Player) {
        if (lineup.none { it.id == player.id }) lineup = lineup + player
    }

    fun removeFromLineup(player: Player) {
        lineup = lineup.filterNot { it.id == player.id }
    }

    /** Moves the player at [index] one seat up or down. */
    fun move(index: Int, up: Boolean) {
        val target = if (up) index - 1 else index + 1
        if (index !in lineup.indices || target !in lineup.indices) return
        lineup = lineup.toMutableList().also {
            val moved = it[index]
            it[index] = it[target]
            it[target] = moved
        }
    }

    /** Adds a brand-new player and seats them at the end of the table. */
    fun addNewPlayer(name: String) {
        viewModelScope.launch {
            if (!playerRepository.add(name)) return@launch
            val refreshed = playerRepository.allPlayers()
            val created = refreshed.firstOrNull { new -> allPlayers.none { it.id == new.id } }
            allPlayers = refreshed
            created?.let { lineup = lineup + it }
        }
    }
}
