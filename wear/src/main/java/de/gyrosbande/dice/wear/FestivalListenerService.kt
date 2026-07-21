package de.gyrosbande.dice.wear

import android.content.ComponentName
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.WearableListenerService
import com.google.android.gms.wearable.DataEventBuffer
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import de.gyrosbande.dice.domain.sync.FestivalSync

/**
 * Receives the Open Flair date the phone syncs (even while the watch app is
 * closed), stores it locally, and nudges the complication to refresh so the
 * countdown on the watch face stays current (phase 3).
 */
class FestivalListenerService : WearableListenerService() {

    override fun onDataChanged(events: DataEventBuffer) {
        var changed = false
        for (event in events) {
            if (event.dataItem.uri.path != FestivalSync.PATH) continue
            val map = DataMapItem.fromDataItem(event.dataItem).dataMap
            val start = map.getLong(FestivalSync.KEY_START, Long.MIN_VALUE)
            val days = map.getInt(FestivalSync.KEY_DAYS, 3)
            FestivalStore(this).save(start.takeIf { it != Long.MIN_VALUE }, days)
            changed = true
        }
        if (changed) {
            ComplicationDataSourceUpdateRequester
                .create(this, ComponentName(this, FestivalComplicationService::class.java))
                .requestUpdateAll()
        }
    }
}
