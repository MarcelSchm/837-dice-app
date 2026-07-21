package de.gyrosbande.dice.domain

/**
 * Where we are relative to the next Open Flair. Computed on plain epoch
 * days (days since 1970-01-01) so the logic stays free of any date library
 * and testable - the platform turns a calendar date into an epoch day.
 */
sealed interface FestivalStatus {
    /** Festival is ahead; [days] until it starts (1 = tomorrow). */
    data class Upcoming(val days: Int) : FestivalStatus

    /** Festival is on right now; [day] is 1-based (day 1 = opening day). */
    data class Running(val day: Int, val totalDays: Int) : FestivalStatus

    /** Festival is over (or was today's last day yesterday). */
    data object Past : FestivalStatus
}

object FestivalCountdown {

    /**
     * @param today epoch day of "now"
     * @param festivalStart epoch day of the festival's first day
     * @param festivalDays how many days it runs (>= 1)
     */
    fun status(today: Long, festivalStart: Long, festivalDays: Int): FestivalStatus {
        require(festivalDays >= 1) { "A festival runs at least one day" }
        val lastDay = festivalStart + festivalDays - 1
        return when {
            today < festivalStart -> FestivalStatus.Upcoming((festivalStart - today).toInt())
            today <= lastDay -> FestivalStatus.Running((today - festivalStart + 1).toInt(), festivalDays)
            else -> FestivalStatus.Past
        }
    }
}
