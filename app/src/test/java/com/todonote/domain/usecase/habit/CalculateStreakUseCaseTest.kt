package com.todonote.domain.usecase.habit

import com.todonote.data.local.entity.HabitCheckInEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.TimeUnit

class CalculateStreakUseCaseTest {

    private val useCase = CalculateStreakUseCase()
    private val today = System.currentTimeMillis()
    private val day = TimeUnit.DAYS.toMillis(1)

    private fun checkIn(date: Long, isComplete: Boolean = true) = HabitCheckInEntity(
        id = 0,
        habitId = 1,
        date = date,
        isComplete = isComplete,
        note = ""
    )

    @Test
    fun `empty checkins returns zero streaks`() {
        val (current, best) = useCase(emptyList())
        assertEquals(0, current)
        assertEquals(0, best)
    }

    @Test
    fun `no completed checkins returns zero streaks`() {
        val checkIns = listOf(
            checkIn(today, isComplete = false)
        )
        val (current, best) = useCase(checkIns)
        assertEquals(0, current)
        assertEquals(0, best)
    }

    @Test
    fun `single completed today gives current streak 1`() {
        val checkIns = listOf(checkIn(today))
        val (current, best) = useCase(checkIns)
        assertEquals(1, current)
        assertEquals(1, best)
    }

    @Test
    fun `three consecutive days gives current streak 3`() {
        val checkIns = listOf(
            checkIn(today),
            checkIn(today - day),
            checkIn(today - 2 * day)
        )
        val (current, best) = useCase(checkIns)
        assertEquals(3, current)
        assertEquals(3, best)
    }

    @Test
    fun `missing today but has yesterday gives current streak from yesterday`() {
        val checkIns = listOf(
            checkIn(today - day),
            checkIn(today - 2 * day)
        )
        val (current, best) = useCase(checkIns)
        assertEquals(2, current)
        assertEquals(2, best)
    }

    @Test
    fun `gap in streak breaks current streak`() {
        val checkIns = listOf(
            checkIn(today),
            checkIn(today - 2 * day),
            checkIn(today - 3 * day)
        )
        val (current, best) = useCase(checkIns)
        assertEquals(1, current)
        assertEquals(2, best)
    }

    @Test
    fun `best streak tracks longest consecutive sequence`() {
        val checkIns = listOf(
            checkIn(today),
            checkIn(today - day),
            checkIn(today - 3 * day),
            checkIn(today - 4 * day),
            checkIn(today - 5 * day)
        )
        val (current, best) = useCase(checkIns)
        assertEquals(2, current)
        assertEquals(3, best)
    }

    @Test
    fun `duplicate dates are deduplicated`() {
        val checkIns = listOf(
            checkIn(today),
            checkIn(today),
            checkIn(today - day)
        )
        val (current, best) = useCase(checkIns)
        assertEquals(2, current)
        assertEquals(2, best)
    }
}
