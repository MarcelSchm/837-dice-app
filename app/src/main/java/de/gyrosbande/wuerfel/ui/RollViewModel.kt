package de.gyrosbande.wuerfel.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gyrosbande.wuerfel.domain.GameFlow
import de.gyrosbande.wuerfel.domain.RollPhase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/** Eingabemodus: App würfelt selbst oder echte Würfel am Tisch. */
enum class RollMode { VIRTUELL, ECHT }

data class RollUiState(
    val phase: RollPhase = RollPhase.CategoryRoll,
    val mode: RollMode = RollMode.VIRTUELL,
    /** Aktuell angezeigte Augenzahlen (auch während der Animation). */
    val shownDice: List<Int> = emptyList(),
    val isRolling: Boolean = false,
    /** Bei manueller Eingabe mit 2 Würfeln: bereits eingetippte Augenzahlen. */
    val pendingManual: List<Int> = emptyList(),
)

class RollViewModel : ViewModel() {

    private val flow = GameFlow()

    var uiState by mutableStateOf(RollUiState())
        private set

    fun setMode(mode: RollMode) {
        if (!uiState.isRolling) uiState = uiState.copy(mode = mode, pendingManual = emptyList())
    }

    /** Virtuell würfeln, mit kurzer Zitter-Animation vor dem Ergebnis. */
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

    /** Eine Augenzahl eines echten Würfels eintippen (1–6). */
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

    /** Neuen Durchgang starten (Modus bleibt erhalten). */
    fun restart() {
        flow.reset()
        uiState = RollUiState(mode = uiState.mode)
    }
}
