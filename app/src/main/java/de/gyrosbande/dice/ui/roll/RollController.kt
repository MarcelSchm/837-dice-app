package de.gyrosbande.dice.ui.roll

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import de.gyrosbande.dice.domain.Category
import de.gyrosbande.dice.domain.GameFlow
import de.gyrosbande.dice.domain.RollPhase
import kotlinx.coroutines.delay
import kotlin.random.Random

/** Input mode: the app rolls for you, or real dice at the table. */
enum class RollMode { VIRTUAL, MANUAL }

data class RollState(
    val phase: RollPhase = RollPhase.CategoryRoll,
    val mode: RollMode = RollMode.VIRTUAL,
    /** Currently displayed pip values (also during the animation). */
    val shownDice: List<Int> = emptyList(),
    val isRolling: Boolean = false,
    /** Manual entry with 2 dice: pip values typed in so far. */
    val pendingManual: List<Int> = emptyList(),
)

/**
 * Drives one [GameFlow] and exposes Compose state for the roll UI.
 * Shared between the quick-roll screen and the round flow, so the rolling
 * experience is identical everywhere.
 */
class RollController(
    categories: List<Category>,
    private val random: Random = Random.Default,
) {
    private val flow = GameFlow(categories, random)

    var state by mutableStateOf(RollState())
        private set

    fun setMode(mode: RollMode) {
        if (!state.isRolling) state = state.copy(mode = mode, pendingManual = emptyList())
    }

    /** Roll virtually, with a short shake animation before the result. */
    suspend fun rollVirtual() {
        if (state.isRolling || flow.phase is RollPhase.Finished) return
        val diceCount = flow.requiredDice()
        state = state.copy(isRolling = true, pendingManual = emptyList())
        repeat(10) {
            state = state.copy(shownDice = List(diceCount) { random.nextInt(1, 7) })
            delay(80)
        }
        val dice = flow.rollVirtual()
        state = state.copy(phase = flow.phase, shownDice = dice, isRolling = false)
    }

    /** Type in one pip value of a real die (1-6). */
    fun enterManualDie(value: Int) {
        if (state.isRolling || flow.phase is RollPhase.Finished) return
        val pending = state.pendingManual + value
        if (pending.size < flow.requiredDice()) {
            state = state.copy(pendingManual = pending, shownDice = pending)
        } else {
            flow.enterManual(pending)
            state = state.copy(phase = flow.phase, shownDice = pending, pendingManual = emptyList())
        }
    }

    /** Start a new roll (mode is kept). */
    fun reset() {
        flow.reset()
        state = RollState(mode = state.mode)
    }
}
