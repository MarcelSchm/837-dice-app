package de.gyrosbande.dice.domain.sync

import de.gyrosbande.dice.domain.Category
import de.gyrosbande.dice.domain.Drink
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Wire format for syncing the drinks menu from the phone to the watch
 * (Wearable Data Layer, phase 2). Deliberately its own set of DTOs rather
 * than serializing the domain [Category]/[Drink] directly, so the
 * on-the-wire shape can evolve independently of the game model - the same
 * split the history export uses.
 *
 * Pure Kotlin (kotlinx-serialization) so it lives in :core and can be
 * reused by the future PWA.
 */
@Serializable
private data class SyncMenu(val version: Int = FORMAT_VERSION, val categories: List<SyncCategory>)

@Serializable
private data class SyncCategory(val diceNumber: Int, val name: String, val drinks: List<SyncDrink>)

@Serializable
private data class SyncDrink(val name: String, val priceCents: Int, val sizeLabel: String? = null)

/** Bump when the wire shape changes incompatibly. */
private const val FORMAT_VERSION = 1

object MenuSync {

    /** Data Layer item path the phone writes and the watch listens on. */
    const val PATH = "/menu"

    /** Key inside the Data Layer item holding the JSON payload. */
    const val KEY_JSON = "menu_json"

    private val json = Json { ignoreUnknownKeys = true }

    /** Serializes the current menu to a compact JSON string. */
    fun encode(categories: List<Category>): String {
        val menu = SyncMenu(
            categories = categories.map { category ->
                SyncCategory(
                    diceNumber = category.diceNumber,
                    name = category.name,
                    drinks = category.drinks.map { SyncDrink(it.name, it.priceCents, it.sizeLabel) },
                )
            },
        )
        return json.encodeToString(menu)
    }

    /**
     * Parses a menu produced by [encode] back into domain categories, order
     * preserved (the drink order is game-relevant - the wrap rule). Returns
     * null if the payload is missing or unreadable, so the watch can fall
     * back to its bundled seed instead of crashing.
     */
    fun decode(text: String?): List<Category>? {
        if (text.isNullOrBlank()) return null
        return try {
            json.decodeFromString<SyncMenu>(text).categories.map { category ->
                Category(
                    diceNumber = category.diceNumber,
                    name = category.name,
                    drinks = category.drinks.map { Drink(it.name, it.priceCents, it.sizeLabel) },
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}
