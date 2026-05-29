package com.todonote.domain.usecase.achievement

import com.todonote.domain.model.Achievement
import com.todonote.domain.repository.IAchievementRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UnlockAchievementUseCaseTest {

    private lateinit var repository: IAchievementRepository
    private lateinit var useCase: UnlockAchievementUseCase

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = UnlockAchievementUseCase(repository)
    }

    private fun achievement(
        id: Long,
        conditionType: String,
        conditionValue: Int,
        isUnlocked: Boolean = false
    ) = Achievement(
        id = id,
        name = "Achievement $id",
        description = "Desc",
        conditionType = conditionType,
        conditionValue = conditionValue,
        isUnlocked = isUnlocked
    )

    @Test
    fun `no achievements returns empty result`() = runTest {
        val stats = UnlockAchievementUseCase.AchievementStats()
        val result = useCase(stats, emptyList())

        assertTrue(result.newlyUnlocked.isEmpty())
    }

    @Test
    fun `already unlocked achievement is skipped`() = runTest {
        val achievements = listOf(
            achievement(1, "TASKS_COMPLETED", 1, isUnlocked = true)
        )
        val stats = UnlockAchievementUseCase.AchievementStats(todayCompletedTasks = 5)

        val result = useCase(stats, achievements)

        assertTrue(result.newlyUnlocked.isEmpty())
        coVerify(exactly = 0) { repository.unlock(any()) }
    }

    @Test
    fun `TASKS_COMPLETED unlocks when threshold met`() = runTest {
        coEvery { repository.unlock(any()) } just Runs

        val achievements = listOf(
            achievement(1, "TASKS_COMPLETED", 5)
        )
        val stats = UnlockAchievementUseCase.AchievementStats(todayCompletedTasks = 5)

        val result = useCase(stats, achievements)

        assertEquals(1, result.newlyUnlocked.size)
        assertTrue(result.newlyUnlocked[0].isUnlocked)
        coVerify { repository.unlock(1L) }
    }

    @Test
    fun `TASKS_COMPLETED does not unlock when below threshold`() = runTest {
        val achievements = listOf(
            achievement(1, "TASKS_COMPLETED", 10)
        )
        val stats = UnlockAchievementUseCase.AchievementStats(todayCompletedTasks = 5)

        val result = useCase(stats, achievements)

        assertTrue(result.newlyUnlocked.isEmpty())
    }

    @Test
    fun `STREAK_DAYS unlocks when any habit meets streak`() = runTest {
        coEvery { repository.unlock(any()) } just Runs

        val achievements = listOf(
            achievement(1, "STREAK_DAYS", 7)
        )
        val stats = UnlockAchievementUseCase.AchievementStats(
            habitStreaks = listOf("Reading" to 3, "Exercise" to 7)
        )

        val result = useCase(stats, achievements)

        assertEquals(1, result.newlyUnlocked.size)
        coVerify { repository.unlock(1L) }
    }

    @Test
    fun `STREAK_DAYS does not unlock when no habit meets streak`() = runTest {
        val achievements = listOf(
            achievement(1, "STREAK_DAYS", 7)
        )
        val stats = UnlockAchievementUseCase.AchievementStats(
            habitStreaks = listOf("Reading" to 3)
        )

        val result = useCase(stats, achievements)

        assertTrue(result.newlyUnlocked.isEmpty())
    }

    @Test
    fun `POMODORO_SESSIONS unlocks when threshold met`() = runTest {
        coEvery { repository.unlock(any()) } just Runs

        val achievements = listOf(
            achievement(1, "POMODORO_SESSIONS", 4)
        )
        val stats = UnlockAchievementUseCase.AchievementStats(todayPomodoroSessions = 4)

        val result = useCase(stats, achievements)

        assertEquals(1, result.newlyUnlocked.size)
        coVerify { repository.unlock(1L) }
    }

    @Test
    fun `TOTAL_FOCUS_MINUTES unlocks when threshold met`() = runTest {
        coEvery { repository.unlock(any()) } just Runs

        val achievements = listOf(
            achievement(1, "TOTAL_FOCUS_MINUTES", 120)
        )
        val stats = UnlockAchievementUseCase.AchievementStats(totalFocusMinutes = 120)

        val result = useCase(stats, achievements)

        assertEquals(1, result.newlyUnlocked.size)
        coVerify { repository.unlock(1L) }
    }

    @Test
    fun `unknown condition type does not unlock`() = runTest {
        val achievements = listOf(
            achievement(1, "UNKNOWN_TYPE", 1)
        )
        val stats = UnlockAchievementUseCase.AchievementStats()

        val result = useCase(stats, achievements)

        assertTrue(result.newlyUnlocked.isEmpty())
    }

    @Test
    fun `multiple achievements unlocks correct ones`() = runTest {
        coEvery { repository.unlock(any()) } just Runs

        val achievements = listOf(
            achievement(1, "TASKS_COMPLETED", 5),
            achievement(2, "TASKS_COMPLETED", 10),
            achievement(3, "STREAK_DAYS", 3)
        )
        val stats = UnlockAchievementUseCase.AchievementStats(
            todayCompletedTasks = 5,
            habitStreaks = listOf("Exercise" to 3)
        )

        val result = useCase(stats, achievements)

        assertEquals(2, result.newlyUnlocked.size)
        assertEquals(1L, result.newlyUnlocked[0].id)
        assertEquals(3L, result.newlyUnlocked[1].id)
    }

    @Test
    fun `unlocked achievement has unlockedAt timestamp`() = runTest {
        coEvery { repository.unlock(any()) } just Runs

        val achievements = listOf(
            achievement(1, "TASKS_COMPLETED", 1)
        )
        val stats = UnlockAchievementUseCase.AchievementStats(todayCompletedTasks = 1)

        val result = useCase(stats, achievements)

        assertTrue(result.newlyUnlocked[0].unlockedAt != null)
        assertTrue(result.newlyUnlocked[0].unlockedAt!! > 0)
    }
}
