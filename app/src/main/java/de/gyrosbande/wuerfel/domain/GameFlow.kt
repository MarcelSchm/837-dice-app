package de.gyrosbande.wuerfel.domain

import kotlin.random.Random

/** Ergebnis eines kompletten Durchgangs (Kategorie + Drink). */
data class RollOutcome(
    val category: Category,
    val drink: Drink,
    val categoryRoll: Int,
    val drinkRolls: List<Int>,
) {
    val drinkRollTotal: Int get() = drinkRolls.sum()
}

/** Die Phasen eines Durchgangs. */
sealed interface RollPhase {
    /** Wurf 1 steht an: Kategorie erwürfeln. */
    data object CategoryRoll : RollPhase

    /** Wurf 2 steht an: Drink in [category] erwürfeln ([diceCount] Würfel). */
    data class DrinkRoll(val category: Category, val categoryRoll: Int) : RollPhase {
        val diceCount: Int get() = DiceRules.diceCountFor(category.drinks.size)
    }

    /** Fertig – [outcome] muss bestellt werden. */
    data class Finished(val outcome: RollOutcome) : RollPhase
}

/**
 * Zustandsmaschine für einen Würfel-Durchgang. Reine Kotlin-Klasse ohne
 * Android-Abhängigkeiten, damit sie komplett testbar ist.
 *
 * Eingaben kommen entweder vom virtuellen Würfel ([rollVirtual]) oder von
 * echten Würfeln am Tisch ([enterManual]).
 */
class GameFlow(
    private val categories: List<Category> = MenuSeed.categories,
    private val random: Random = Random.Default,
) {
    var phase: RollPhase = RollPhase.CategoryRoll
        private set

    fun reset() {
        phase = RollPhase.CategoryRoll
    }

    /** Wie viele Würfel die aktuelle Phase braucht. */
    fun requiredDice(): Int = when (val p = phase) {
        is RollPhase.CategoryRoll -> 1
        is RollPhase.DrinkRoll -> p.diceCount
        is RollPhase.Finished -> 0
    }

    /** Virtuell würfeln: Die App würfelt selbst. Gibt die Augenzahlen zurück. */
    fun rollVirtual(): List<Int> {
        val dice = List(requiredDice()) { random.nextInt(1, 7) }
        check(dice.isNotEmpty()) { "Durchgang ist bereits beendet" }
        advance(dice)
        return dice
    }

    /**
     * Ergebnis echter Würfel eintragen. [dice] sind die einzelnen Augenzahlen
     * (1–6); bei zwei Würfeln also zwei Werte.
     */
    fun enterManual(dice: List<Int>) {
        require(dice.size == requiredDice()) {
            "Erwartet ${requiredDice()} Würfel, bekommen: ${dice.size}"
        }
        require(dice.all { it in 1..6 }) { "Augenzahlen müssen 1–6 sein: $dice" }
        advance(dice)
    }

    private fun advance(dice: List<Int>) {
        when (val p = phase) {
            is RollPhase.CategoryRoll -> {
                val roll = dice.single()
                val category = categories.first { it.diceNumber == roll }
                phase = RollPhase.DrinkRoll(category, roll)
            }
            is RollPhase.DrinkRoll -> {
                val total = dice.sum()
                val drink = DiceRules.drinkFor(p.category, total)
                phase = RollPhase.Finished(
                    RollOutcome(
                        category = p.category,
                        drink = drink,
                        categoryRoll = p.categoryRoll,
                        drinkRolls = dice,
                    )
                )
            }
            is RollPhase.Finished -> error("Durchgang ist bereits beendet")
        }
    }
}
