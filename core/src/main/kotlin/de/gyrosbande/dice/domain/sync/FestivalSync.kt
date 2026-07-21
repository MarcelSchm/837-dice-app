package de.gyrosbande.dice.domain.sync

/**
 * Data Layer contract for mirroring the next Open Flair date to the watch,
 * so a watch-face complication can count down to it (phase 3). The value is
 * just two numbers, so no JSON is needed - they go straight into the data
 * map under these keys.
 */
object FestivalSync {

    /** Data Layer item path the phone writes the festival date to. */
    const val PATH = "/festival"

    /** Epoch day of the festival's first day (missing when unset). */
    const val KEY_START = "festival_start"

    /** How many days the festival runs. */
    const val KEY_DAYS = "festival_days"
}
