package com.todonote.domain.usecase.habit

import com.todonote.domain.model.Frequency
import com.todonote.domain.model.Habit
import com.todonote.domain.repository.IHabitRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitUseCasesTest {

    private lateinit var repository: IHabitRepository
    private val testHabit = Habit(
        id = 1,
        name = "Test Habit",
        color = 0xFF0000,
        frequency = Frequency.DAILY
    )

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `createHabit calls repository insert and returns id`() = runTest {
        coEvery { repository.insert(any()) } returns 42L

        val useCase = CreateHabitUseCase(repository)
        val result = useCase(testHabit)

        assertEquals(42L, result)
        coVerify { repository.insert(testHabit) }
    }

    @Test
    fun `getHabits returns flow from repository`() = runTest {
        val habits = listOf(testHabit)
        coEvery { repository.getActive() } returns flowOf(habits)

        val useCase = GetHabitsUseCase(repository)
        val result = useCase().first()

        assertEquals(habits, result)
    }

    @Test
    fun `updateHabit calls repository update`() = runTest {
        coEvery { repository.update(any()) } just Runs

        val useCase = UpdateHabitUseCase(repository)
        useCase(testHabit.copy(name = "Updated"))

        coVerify { repository.update(any()) }
    }

    @Test
    fun `deleteHabit calls repository delete`() = runTest {
        coEvery { repository.delete(any()) } just Runs

        val useCase = DeleteHabitUseCase(repository)
        useCase(testHabit)

        coVerify { repository.delete(testHabit) }
    }
}
