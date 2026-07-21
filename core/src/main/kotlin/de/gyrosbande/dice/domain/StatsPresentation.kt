package de.gyrosbande.dice.domain

/** One hall-of-fame line, ready to show or draw. */
data class FunFactLine(
    val emoji: String,
    val title: String,
    val holder: String,
    val detail: String,
)

/**
 * Turns [Stats] into the German hall-of-fame lines. Shared by the on-screen
 * cards and the shareable image, so both always read identically. Lines
 * that have no data are simply left out.
 */
object StatsPresentation {

    fun funFacts(stats: Stats): List<FunFactLine> = buildList {
        stats.proseccoKing?.let {
            add(FunFactLine("🍾", "Prosecco-König:in", it.names.joinToString(" & "),
                "${it.count}× die Flasche Prosecco erwischt"))
        }
        stats.topSpender?.let {
            add(FunFactLine("💸", "Spendierhosen", it.names.joinToString(" & "),
                "insgesamt ${OrderSummary.formatCents(it.cents)} verwürfelt"))
        }
        stats.favoriteDrink?.let {
            add(FunFactLine("🥃", "Stammgetränk der Bande",
                it.drinkName + (it.sizeLabel?.let { s -> " ($s)" } ?: ""),
                "${it.count}× gewürfelt"))
        }
        stats.categoryMagnet?.let {
            add(FunFactLine("🎯", "Kategorien-Magnet", it.names.joinToString(" & "),
                "${it.count}× in ${it.categoryName} gelandet"))
        }
        stats.doublesChamp?.let {
            add(FunFactLine("🎲", "Pasch-Profi", it.names.joinToString(" & "),
                if (it.count == 1) "1 Pasch beim Drink-Wurf" else "${it.count} Päsche beim Drink-Wurf"))
        }
        stats.wrapVictim?.let {
            add(FunFactLine("🔄", "Wrap-Opfer", it.names.joinToString(" & "),
                "${it.count}× unten durch und oben wieder reingerutscht"))
        }
    }

    /** "3 Runden, 12 Würfe" - the tally line, with singular/plural. */
    fun tally(stats: Stats): String {
        val rounds = if (stats.roundCount == 1) "Runde" else "Runden"
        val rolls = if (stats.rollCount == 1) "Wurf" else "Würfe"
        return "${stats.roundCount} $rounds, ${stats.rollCount} $rolls"
    }

    fun revenueLine(stats: Stats): String =
        "Ihr habt dem San Remo schon ${stats.totalFormatted} beschert 🇬🇷"
}
