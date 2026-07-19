package de.gyrosbande.dice.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gyrosbande.dice.domain.GameFlow
import de.gyrosbande.dice.domain.RollPhase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Input mode: the app rolls for you, or real dice at the table. */
enum class RollMode { VIRTUAL, MANUAL }

data class RollUiState(
    val phase: RollPhase = RollPhase.CategoryRoll,
    val mode: RollMode = RollMode.VIRTUAL,
    /** Currently displayed pip values (also during the animation). */
    val shownDice: List<Int> = emptyList(),
    val isRolling: Boolean = false,
    /** Manual entry with 2 dice: pip values typed in so far. */
    val pendingManual: List<Int> = emptyList(),
)

class RollViewModel : ViewModel() {

    private val flow = GameFlow()

    var uiState by mutableStateOf(RollUiState())
        private set

    fun setMode(mode: RollMode) {
        if (!uiState.isRolling) uiState = uiState.copy(mode = mode, pendingManual = emptyList())
    }

    /** Roll virtually, with a short shake animation before the result. */
    fun rollVirtual() {
        if (uiState.isRolling || flow.phase is RollPhase.Finished) return
        val diceCount = flow.requiredDice()
        viewModelScope.launch {
            uiState = uiState.copy(isRolling = true, pendingManual = emptyList())
            repeat(10) {
                uiState = uiState.copy(shownDice = List(diceCount) { Random.nextInt(1, 7) })
                delay(80)
            }
            val dice = flow.rollVirtual()
            uiState = uiState.copy(phase = flow.phase, shownDice = dice, isRolling = false)
        }
    }

    /** Type in one pip value of a real die (1-6). */
    fun enterManualDie(value: Int) {
        if (uiState.isRolling || flow.phase is RollPhase.Finished) return
        val pending = uiState.pendingManual + value
        if (pending.size < flow.requiredDice()) {
            uiState = uiState.copy(pendingManual = pending, shownDice = pending)
        } else {
            flow.enterManual(pending)
            uiState = uiState.copy(phase = flow.phase, shownDice = pending, pendingManual = emptyList())
        }
    }

    /** Start a new round (mode is kept). */
    fun restart() {
        flow.reset()
        uiState = RollUiState(mode = uiState.mode)
    }
}
