package de.gyrosbande.dice.wear

import android.content.Context

/**
 * The last Open Flair date the phone synced, kept locally so the complication
 * can read it instantly (phase 3). [startEpochDay] is null until a date has
 * been synced.
 */
class FestivalStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("festival", Context.MODE_PRIVATE)

    val startEpochDay: Long?
        get() = prefs.getLong(KEY_START, NOT_SET).takeIf { it != NOT_SET }

    val days: Int
        get() = prefs.getInt(KEY_DAYS, DEFAULT_DAYS)

    fun save(startEpochDay: Long?, days: Int) {
        prefs.edit()
            .putLong(KEY_START, startEpochDay ?: NOT_SET)
            .putInt(KEY_DAYS, days)
            .apply()
    }

    private companion object {
        const val KEY_START = "start"
        const val KEY_DAYS = "days"
        const val NOT_SET = Long.MIN_VALUE
        const val DEFAULT_DAYS = 3
    }
}
