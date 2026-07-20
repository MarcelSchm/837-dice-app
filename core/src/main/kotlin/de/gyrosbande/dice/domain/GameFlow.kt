package de.gyrosbande.dice.domain

import kotlin.random.Random

/** Result of one complete round (category + drink). */
data class RollOutcome(
    val category: Category,
    val drink: Drink,
    val categoryRoll: Int,
    val drinkRolls: List<Int>,
    /** True when the rolled drink was unavailable and replaced by hand. */
    val substituted: Boolean = false,
) {
    val drinkRollTotal: Int get() = drinkRolls.sum()
}

/** The phases of one round. */
sealed interface RollPhase {
    /** Roll 1 is up: roll the category. */
    data object CategoryRoll : RollPhase

    /** Roll 2 is up: roll the drink within [category] ([diceCount] dice). */
    data class DrinkRoll(val category: Category, val categoryRoll: Int) : RollPhase {
        val diceCount: Int get() = DiceRules.diceCountFor(category.drinks.size)
    }

    /** Done - [outcome] must be ordered. */
    data class Finished(val outcome: RollOutcome) : RollPhase
}

/**
 * State machine for one dice round. Pure Kotlin class without Android
 * dependencies so it is fully unit-testable.
 *
 * Input comes either from the virtual dice ([rollVirtual]) or from real
 * dice at the table ([enterManual]).
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

    /** How many dice the current phase requires. */
    fun requiredDice(): Int = when (val p = phase) {
        is RollPhase.CategoryRoll -> 1
        is RollPhase.DrinkRoll -> p.diceCount
        is RollPhase.Finished -> 0
    }

    /** Roll virtually: the app rolls for you. Returns the pip values. */
    fun rollVirtual(): List<Int> {
        val dice = List(requiredDice()) { random.nextInt(1, 7) }
        check(dice.isNotEmpty()) { "Round is already finished" }
        advance(dice)
        return dice
    }

    /**
     * San Remo is out of the rolled drink: roll the drink again within the
     * same category (that's the house rule - the category stays).
     */
    fun rerollDrink() {
        val p = phase as? RollPhase.Finished ?: error("No finished roll to reroll")
        phase = RollPhase.DrinkRoll(p.outcome.category, p.outcome.categoryRoll)
    }

    /**
     * San Remo is out of the rolled drink: replace it by hand (e.g. Grog
     * instead of Glühwein). The original rolls stay recorded, the outcome
     * is marked as substituted.
     */
    fun substitute(drink: Drink) {
        val p = phase as? RollPhase.Finished ?: error("No finished roll to substitute")
        phase = RollPhase.Finished(p.outcome.copy(drink = drink, substituted = true))
    }

    /**
     * Enter the result of real dice. [dice] are the individual pip values
     * (1-6); with two dice that means two values.
     */
    fun enterManual(dice: List<Int>) {
        require(dice.size == requiredDice()) {
            "Expected ${requiredDice()} dice, got: ${dice.size}"
        }
        require(dice.all { it in 1..6 }) { "Pip values must be 1-6: $dice" }
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
            is RollPhase.Finished -> error("Round is already finished")
        }
    }
}
