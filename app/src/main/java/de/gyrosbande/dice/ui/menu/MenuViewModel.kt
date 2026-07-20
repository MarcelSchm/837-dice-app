package de.gyrosbande.dice.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gyrosbande.dice.data.EditableCategory
import de.gyrosbande.dice.data.MenuRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MenuViewModel(private val menuRepository: MenuRepository) : ViewModel() {

    val categories: StateFlow<List<EditableCategory>> = menuRepository.observeEditableMenu()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch { menuRepository.ensureSeeded() }
    }

    fun renameCategory(categoryId: Long, name: String) {
        viewModelScope.launch { menuRepository.renameCategory(categoryId, name) }
    }

    fun swapDiceNumber(categoryId: Long, newNumber: Int) {
        viewModelScope.launch { menuRepository.swapDiceNumber(categoryId, newNumber) }
    }

    fun addDrink(categoryId: Long, name: String, priceCents: Int, sizeLabel: String?) {
        viewModelScope.launch { menuRepository.addDrink(categoryId, name, priceCents, sizeLabel) }
    }

    fun updateDrink(drinkId: Long, name: String, priceCents: Int, sizeLabel: String?) {
        viewModelScope.launch { menuRepository.updateDrink(drinkId, name, priceCents, sizeLabel) }
    }

    fun deleteDrink(drinkId: Long) {
        viewModelScope.launch { menuRepository.deleteDrink(drinkId) }
    }

    fun moveDrink(drinkId: Long, up: Boolean) {
        viewModelScope.launch { menuRepository.moveDrink(drinkId, up) }
    }

    fun resetMenu() {
        viewModelScope.launch { menuRepository.resetToSeed() }
    }
}
