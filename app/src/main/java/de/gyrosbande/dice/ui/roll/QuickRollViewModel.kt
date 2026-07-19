package de.gyrosbande.dice.ui.roll

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gyrosbande.dice.data.MenuRepository
import kotlinx.coroutines.launch

/** Quick roll without players or persistence - just "what do I order?". */
class QuickRollViewModel(menuRepository: MenuRepository) : ViewModel() {

    /** Null while the menu is loading from the database. */
    var controller by mutableStateOf<RollController?>(null)
        private set

    init {
        viewModelScope.launch {
            controller = RollController(menuRepository.categories())
        }
    }

    fun rollVirtual() {
        viewModelScope.launch { controller?.rollVirtual() }
    }
}
