package de.gyrosbande.dice.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import de.gyrosbande.dice.domain.Category
import de.gyrosbande.dice.domain.sync.MenuSync
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds the drinks menu the phone last synced over the Wearable Data Layer
 * (phase 2). [categories] is null until a menu has arrived; the UI then
 * falls back to the bundled seed, so the watch always works even without a
 * paired phone.
 */
class WatchMenu(context: Context) {

    private val dataClient = Wearable.getDataClient(context)

    private val _categories = MutableStateFlow<List<Category>?>(null)
    val categories: StateFlow<List<Category>?> = _categories

    private val listener = DataClient.OnDataChangedListener { events ->
        for (event in events) {
            if (event.dataItem.uri.path != MenuSync.PATH) continue
            _categories.value = when (event.type) {
                DataEvent.TYPE_CHANGED -> read(event.dataItem)
                DataEvent.TYPE_DELETED -> null
                else -> _categories.value
            }
        }
    }

    /** Starts listening and pulls whatever menu is already on the device. */
    fun start() {
        dataClient.addListener(listener)
        dataClient.dataItems.addOnSuccessListener { buffer ->
            try {
                buffer.firstOrNull { it.uri.path == MenuSync.PATH }
                    ?.let { _categories.value = read(it) }
            } finally {
                buffer.release()
            }
        }.addOnFailureListener { Log.d(TAG, "No synced menu yet: ${it.message}") }
    }

    fun stop() {
        dataClient.removeListener(listener)
    }

    private fun read(item: com.google.android.gms.wearable.DataItem): List<Category>? {
        val json = DataMapItem.fromDataItem(item).dataMap.getString(MenuSync.KEY_JSON)
        return MenuSync.decode(json)
    }

    private companion object {
        const val TAG = "WatchMenu"
    }
}
