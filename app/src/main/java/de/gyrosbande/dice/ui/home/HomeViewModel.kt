package de.gyrosbande.dice.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.gyrosbande.dice.data.SettingsRepository
import de.gyrosbande.dice.domain.FestivalCountdown
import de.gyrosbande.dice.domain.FestivalStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate

class HomeViewModel(private val settings: SettingsRepository) : ViewModel() {

    /** Countdown state for the home banner; null until a date is set. */
    val festivalStatus: StateFlow<FestivalStatus?> =
        settings.festivalStartEpochDay
            .map { start ->
                start?.let {
                    FestivalCountdown.status(
                        today = LocalDate.now().toEpochDay(),
                        festivalStart = it,
                        festivalDays = settings.festivalDays,
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** The stored festival start, for pre-selecting the date picker. */
    val festivalStartEpochDay: StateFlow<Long?> = settings.festivalStartEpochDay

    fun setFestivalStart(epochDay: Long) = settings.setFestivalStart(epochDay)

    fun clearFestivalStart() = settings.setFestivalStart(null)
}
