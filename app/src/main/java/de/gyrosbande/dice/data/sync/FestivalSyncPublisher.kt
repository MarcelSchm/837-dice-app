package de.gyrosbande.dice.data.sync

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import de.gyrosbande.dice.data.SettingsRepository
import de.gyrosbande.dice.domain.sync.FestivalSync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Mirrors the next Open Flair date to a paired watch (phase 3), so a
 * watch-face complication can count down to it. Best-effort: no watch or no
 * Play Services simply means nothing is published.
 */
class FestivalSyncPublisher(
    context: Context,
    private val settingsRepository: SettingsRepository,
) {
    private val appContext = context.applicationContext
    private val dataClient by lazy { Wearable.getDataClient(appContext) }

    fun start(scope: CoroutineScope) {
        scope.launch {
            // StateFlow already only emits on change.
            settingsRepository.festivalStartEpochDay
                .collect { start -> publish(start, settingsRepository.festivalDays) }
        }
    }

    private fun publish(startEpochDay: Long?, days: Int) {
        try {
            val request = PutDataMapRequest.create(FestivalSync.PATH).apply {
                // Long.MIN_VALUE means "unset" - the watch treats it as no date.
                dataMap.putLong(FestivalSync.KEY_START, startEpochDay ?: Long.MIN_VALUE)
                dataMap.putInt(FestivalSync.KEY_DAYS, days)
            }.asPutDataRequest().setUrgent()
            dataClient.putDataItem(request)
                .addOnFailureListener { Log.d(TAG, "Festival sync skipped: ${it.message}") }
        } catch (e: Exception) {
            Log.d(TAG, "Festival sync skipped: ${e.message}")
        }
    }

    private companion object {
        const val TAG = "FestivalSyncPublisher"
    }
}
