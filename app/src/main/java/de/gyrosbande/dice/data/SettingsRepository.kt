package de.gyrosbande.dice.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Small app-wide settings kept in SharedPreferences. Right now just the
 * next Open Flair date for the home-screen countdown (as an epoch day, so
 * it is timezone-agnostic).
 */
class SettingsRepository(context: Context) {

    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _festivalStartEpochDay = MutableStateFlow(
        prefs.getLong(KEY_FESTIVAL_START, NOT_SET).takeIf { it != NOT_SET }
    )
    /** Epoch day of the festival's first day, or null when unset. */
    val festivalStartEpochDay: StateFlow<Long?> = _festivalStartEpochDay.asStateFlow()

    /** Open Flair runs Friday to Sunday - three days. */
    val festivalDays: Int = 3

    fun setFestivalStart(epochDay: Long?) {
        prefs.edit().apply {
            if (epochDay == null) remove(KEY_FESTIVAL_START) else putLong(KEY_FESTIVAL_START, epochDay)
        }.apply()
        _festivalStartEpochDay.value = epochDay
    }

    private companion object {
        const val KEY_FESTIVAL_START = "festivalStartEpochDay"
        const val NOT_SET = Long.MIN_VALUE
    }
}
