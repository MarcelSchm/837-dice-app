package de.gyrosbande.dice.wear

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import androidx.wear.watchface.complications.datasource.SuspendingComplicationDataSourceService
import de.gyrosbande.dice.domain.FestivalCountdown
import de.gyrosbande.dice.domain.FestivalStatus
import java.time.LocalDate

/**
 * A watch-face complication that counts down to the next Open Flair
 * (phase 3). Reuses the shared [FestivalCountdown] logic and the date the
 * phone synced ([FestivalStore]); shows "10T" before, "Tag 2" during, "🎪"
 * after, or a dash until a date has been synced.
 */
class FestivalComplicationService : SuspendingComplicationDataSourceService() {

    override fun getPreviewData(type: ComplicationType): ComplicationData =
        complication("10T")

    override suspend fun onComplicationRequest(request: ComplicationRequest): ComplicationData =
        complication(countdownText())

    private fun countdownText(): String {
        val start = FestivalStore(this).startEpochDay ?: return "–"
        val today = LocalDate.now().toEpochDay()
        return when (val status = FestivalCountdown.status(today, start, FestivalStore(this).days)) {
            is FestivalStatus.Upcoming -> "${status.days}T"
            is FestivalStatus.Running -> "Tag ${status.day}"
            FestivalStatus.Past -> "🎪"
        }
    }

    private fun complication(text: String): ShortTextComplicationData =
        ShortTextComplicationData.Builder(
            text = PlainComplicationText.Builder(text).build(),
            contentDescription = PlainComplicationText.Builder("Countdown bis Open Flair").build(),
        )
            .setTitle(PlainComplicationText.Builder("Flair").build())
            .build()
}
