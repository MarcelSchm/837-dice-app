package de.gyrosbande.dice.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import de.gyrosbande.dice.domain.sync.RoundSync
import de.gyrosbande.dice.domain.sync.WatchRoundState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Watch side of a connected round (phase 2b). A passive live display: it
 * listens for the round state the phone publishes and shows it, but never
 * drives the round - all rolling happens in the phone app. When no round is
 * active, [state] is [WatchRoundState.INACTIVE] and the watch shows its
 * standalone quick-roll.
 */
class WatchRound(context: Context) {

    private val appContext = context.applicationContext
    private val dataClient = Wearable.getDataClient(appContext)

    private val _state = MutableStateFlow(WatchRoundState.INACTIVE)
    val state: StateFlow<WatchRoundState> = _state

    private val listener = DataClient.OnDataChangedListener { events ->
        for (event in events) {
            if (event.dataItem.uri.path != RoundSync.PATH) continue
            _state.value = when (event.type) {
                DataEvent.TYPE_CHANGED -> read(event.dataItem)
                DataEvent.TYPE_DELETED -> WatchRoundState.INACTIVE
                else -> _state.value
            }
        }
    }

    fun start() {
        dataClient.addListener(listener)
        dataClient.dataItems.addOnSuccessListener { buffer ->
            try {
                buffer.firstOrNull { it.uri.path == RoundSync.PATH }
                    ?.let { _state.value = read(it) }
            } finally {
                buffer.release()
            }
        }.addOnFailureListener { Log.d(TAG, "No round yet: ${it.message}") }
    }

    fun stop() {
        dataClient.removeListener(listener)
    }

    private fun read(item: com.google.android.gms.wearable.DataItem): WatchRoundState {
        val json = DataMapItem.fromDataItem(item).dataMap.getString(RoundSync.KEY_JSON)
        return RoundSync.decode(json)
    }

    private companion object {
        const val TAG = "WatchRound"
    }
}
