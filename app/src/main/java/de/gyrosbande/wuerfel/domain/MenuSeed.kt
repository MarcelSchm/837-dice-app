package de.gyrosbande.wuerfel.domain

/**
 * Die Getränkekarte vom San Remo (Stand 2026), abgetippt von den Fotos.
 * Reihenfolge = Kartenreihenfolge, sie ist spielentscheidend (Wrap-Regel).
 *
 * Wird später durch die editierbare Room-Datenbank ersetzt; diese Liste
 * dient dann als Seed beim ersten App-Start.
 */
object MenuSeed {

    val categories: List<Category> = listOf(
        Category(
            diceNumber = 1,
            name = "Schnäpse & Brände",
            drinks = listOf(
                Drink("Ouzo 38 %", 250, "2 cl"),
                Drink("Grappa 40 %", 250, "2 cl"),
                Drink("Obstler 38 %", 250, "2 cl"),
                Drink("Linie Aquavit 41,5 %", 250, "2 cl"),
                Drink("Malteser 40 %", 250, "2 cl"),
                Drink("Fürst Bismark 38 %", 250, "2 cl"),
                Drink("Wodka 38 %", 250, "2 cl"),
            ),
        ),
        Category(
            diceNumber = 2,
            name = "Rum & Spezial",
            drinks = listOf(
                Drink("Bacardi 37,5 %", 250, "2 cl"),
                Drink("Flasche Prosecco", 1350, "0,7 l"),
                Drink("Glas Prosecco", 290, "0,1 l"),
                Drink("Grog", 300),
                Drink("Glühwein", 300),
            ),
        ),
        Category(
            diceNumber = 3,
            name = "Bitter",
            drinks = listOf(
                Drink("Ramazotti 30 %", 250, "2 cl"),
                Drink("Averna 32 %", 250, "2 cl"),
                Drink("Jägermeister 35 %", 250, "2 cl"),
                Drink("Fernet Branca 42 %", 250, "2 cl"),
            ),
        ),
        Category(
            diceNumber = 4,
            name = "Likör",
            drinks = listOf(
                Drink("Sambuca 40 %", 250, "2 cl"),
                Drink("Amaretto 21,5 %", 250, "2 cl"),
                Drink("Marsala 15 %", 250, "2 cl"),
            ),
        ),
        Category(
            diceNumber = 5,
            name = "Whisky Longdrink",
            drinks = listOf(
                Drink("Johnnie Walker 40 %", 350),
                Drink("Jim Beam 40 %", 350),
                Drink("Jack Daniels 43 %", 400),
                Drink("Chevas Regal 40 %", 400),
            ),
        ),
        Category(
            diceNumber = 6,
            name = "Weinbrand & Cognac",
            drinks = listOf(
                Drink("Mariacron 36 %", 250, "2 cl"),
                Drink("Asbach Uralt 38 %", 250, "2 cl"),
                Drink("Veccia Romagna 38 %", 250, "2 cl"),
                Drink("Metaxa 5 Sterne 38 %", 250, "2 cl"),
            ),
        ),
    )

    fun categoryFor(diceNumber: Int): Category =
        categories.first { it.diceNumber == diceNumber }
}
