package de.gyrosbande.dice.domain

/**
 * The binding dice rules of the 837 Gyrosbande (see CLAUDE.md):
 *
 * 1. Category roll: one die (1-6) picks the category.
 * 2. Drink roll: up to 6 drinks one die, 7 or more two dice (sum counts).
 * 3. Wrap rule: if the roll is greater than the number of drinks, counting
 *    continues at the top of the list ("off the bottom -> back to the top").
 */
object DiceRules {

    /** Number of dice for the drink roll within a category. */
    fun diceCountFor(drinkCount: Int): Int {
        require(drinkCount > 0) { "Category without drinks" }
        return if (drinkCount > 6) 2 else 1
    }

    /** Valid value range of a roll with [diceCount] dice. */
    fun rollRange(diceCount: Int): IntRange = diceCount..(diceCount * 6)

    /**
     * Applies the wrap rule: roll [roll] against a list of [itemCount]
     * entries yields the 0-based index.
     * Example: 7 schnapps, sum 9 -> 9 - 7 = 2 -> index 1 (Grappa).
     */
    fun resolveIndex(roll: Int, itemCount: Int): Int {
        require(itemCount > 0) { "Empty list" }
        require(roll >= 1) { "Invalid roll: $roll" }
        return (roll - 1) % itemCount
    }

    /** The rolled drink of a category, wrap rule included. */
    fun drinkFor(category: Category, roll: Int): Drink =
        category.drinks[resolveIndex(roll, category.drinks.size)]
}
