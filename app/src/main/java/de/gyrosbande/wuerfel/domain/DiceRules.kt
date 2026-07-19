package de.gyrosbande.wuerfel.domain

/**
 * Die verbindlichen Würfelregeln der 837 Gyrosbande (siehe CLAUDE.md):
 *
 * 1. Kategorie-Wurf: ein Würfel (1–6) wählt die Kategorie.
 * 2. Drink-Wurf: bis 6 Drinks ein Würfel, ab 7 Drinks zwei Würfel (Summe).
 * 3. Wrap-Regel: Ist der Wurf größer als die Anzahl der Drinks, wird oben
 *    in der Liste weitergezählt („unten durch → oben weiter").
 */
object DiceRules {

    /** Anzahl Würfel für den Drink-Wurf in einer Kategorie. */
    fun diceCountFor(drinkCount: Int): Int {
        require(drinkCount > 0) { "Kategorie ohne Drinks" }
        return if (drinkCount > 6) 2 else 1
    }

    /** Gültiger Wertebereich eines Wurfs mit [diceCount] Würfeln. */
    fun rollRange(diceCount: Int): IntRange = diceCount..(diceCount * 6)

    /**
     * Wendet die Wrap-Regel an: Wurf [roll] auf eine Liste mit [itemCount]
     * Einträgen ergibt den 0-basierten Index.
     * Beispiel: 7 Schnäpse, Summe 9 → 9 − 7 = 2 → Index 1 (Grappa).
     */
    fun resolveIndex(roll: Int, itemCount: Int): Int {
        require(itemCount > 0) { "Leere Liste" }
        require(roll >= 1) { "Ungültiger Wurf: $roll" }
        return (roll - 1) % itemCount
    }

    /** Der erwürfelte Drink einer Kategorie inklusive Wrap-Regel. */
    fun drinkFor(category: Category, roll: Int): Drink =
        category.drinks[resolveIndex(roll, category.drinks.size)]
}
