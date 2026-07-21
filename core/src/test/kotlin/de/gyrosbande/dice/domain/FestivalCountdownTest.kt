package de.gyrosbande.dice.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class FestivalCountdownTest {

    // An arbitrary epoch day standing in for the festival's opening day.
    private val start = 20_678L // 2026-08-14
    private val days = 3

    @Test
    fun `counts the days until the festival`() {
        assertEquals(FestivalStatus.Upcoming(10), FestivalCountdown.status(start - 10, start, days))
        assertEquals(FestivalStatus.Upcoming(1), FestivalCountdown.status(start - 1, start, days))
    }

    @Test
    fun `opening day is running day one`() {
        assertEquals(FestivalStatus.Running(1, 3), FestivalCountdown.status(start, start, days))
    }

    @Test
    fun `middle and last day are still running`() {
        assertEquals(FestivalStatus.Running(2, 3), FestivalCountdown.status(start + 1, start, days))
        assertEquals(FestivalStatus.Running(3, 3), FestivalCountdown.status(start + 2, start, days))
    }

    @Test
    fun `the day after the last day is past`() {
        assertEquals(FestivalStatus.Past, FestivalCountdown.status(start + 3, start, days))
        assertEquals(FestivalStatus.Past, FestivalCountdown.status(start + 100, start, days))
    }

    @Test
    fun `a one-day festival runs only on its day`() {
        assertEquals(FestivalStatus.Running(1, 1), FestivalCountdown.status(start, start, 1))
        assertEquals(FestivalStatus.Past, FestivalCountdown.status(start + 1, start, 1))
    }

    @Test
    fun `zero-day festival is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            FestivalCountdown.status(start, start, 0)
        }
    }
}
