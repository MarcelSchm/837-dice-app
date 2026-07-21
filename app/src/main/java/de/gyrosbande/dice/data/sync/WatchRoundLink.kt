package de.gyrosbande.dice.data.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import de.gyrosbande.dice.domain.sync.RoundSync
import de.gyrosbande.dice.domain.sync.WatchRoundState

/**
 * One-way mirror of a live round to a paired watch (phase 2b). The watch is
 * a passive second display - it shows whose turn it is and the rolled drink,
 * but never drives the round: all rolling happens in the phone app, so the
 * watch can be jostled without any effect.
 *
 * Screen-scoped: the round ViewModel publishes while the round screen is up
 * and [close]s it afterwards, which clears the round so the watch drops back
 * to standalone quick-roll. Best-effort - no watch or no Play Services simply
 * means nothing is shown.
 */
class WatchRoundLink(context: Context) {

    private val appContext = context.applicationContext
    private val dataClient by lazy { Wearable.getDataClient(appContext) }

    /** Mirrors [state] to the watch. Identical states are dropped by the Data Layer. */
    fun publish(state: WatchRoundState) {
        try {
            val request = PutDataMapRequest.create(RoundSync.PATH).apply {
                dataMap.putString(RoundSync.KEY_JSON, RoundSync.encode(state))
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request)
                .addOnFailureListener { Log.d(TAG, "Round publish skipped: ${it.message}") }
        } catch (e: Exception) {
            Log.d(TAG, "Round publish skipped: ${e.message}")
        }
    }

    /** Tells the watch the round is over. */
    fun close() {
        publish(WatchRoundState.INACTIVE)
    }

    private companion object {
        const val TAG = "WatchRoundLink"
    }
}
