package de.gyrosbande.dice.data.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import de.gyrosbande.dice.data.MenuRepository
import de.gyrosbande.dice.domain.Category
import de.gyrosbande.dice.domain.Drink
import de.gyrosbande.dice.domain.sync.MenuSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Pushes the current drinks menu to any paired watch via the Wearable
 * Data Layer, so the watch rolls on the same (edited) card as the phone
 * instead of its bundled seed (phase 2 - see docs/WEAR.md).
 *
 * The phone stays the source of truth: it just mirrors the menu outward.
 * Everything is best-effort - if there is no watch, no Play Services, or
 * the push fails, the phone app carries on unaffected.
 */
class MenuSyncPublisher(
    private val context: Context,
    private val menuRepository: MenuRepository,
) {
    private val dataClient by lazy { Wearable.getDataClient(context) }

    /**
     * Observes the menu for the process lifetime and mirrors every change
     * to the Data Layer. Identical menus produce an identical data item,
     * which the Data Layer drops as a no-op, so this does not spam updates.
     */
    fun start(scope: CoroutineScope) {
        scope.launch {
            menuRepository.observeEditableMenu()
                .map { editable ->
                    editable.map { category ->
                        Category(
                            diceNumber = category.diceNumber,
                            name = category.name,
                            drinks = category.drinks.map { Drink(it.name, it.priceCents, it.sizeLabel) },
                        )
                    }
                }
                .distinctUntilChanged()
                .collect { categories -> publish(categories) }
        }
    }

    private fun publish(categories: List<Category>) {
        try {
            val request = PutDataMapRequest.create(MenuSync.PATH).apply {
                dataMap.putString(MenuSync.KEY_JSON, MenuSync.encode(categories))
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request)
                .addOnFailureListener { Log.d(TAG, "Menu sync skipped: ${it.message}") }
        } catch (e: Exception) {
            // No watch / no Play Services / transient failure - ignore.
            Log.d(TAG, "Menu sync skipped: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "MenuSyncPublisher"
    }
}
