package de.gyrosbande.dice.ui.players

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gyrosbande.dice.data.PlayerRepository
import de.gyrosbande.dice.domain.Player
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlayersViewModel(private val playerRepository: PlayerRepository) : ViewModel() {

    val players: StateFlow<List<Player>> = playerRepository.observePlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun add(name: String) {
        viewModelScope.launch { playerRepository.add(name) }
    }

    fun setActive(player: Player, active: Boolean) {
        viewModelScope.launch { playerRepository.setActive(player, active) }
    }

    fun remove(player: Player) {
        viewModelScope.launch { playerRepository.remove(player) }
    }
}
