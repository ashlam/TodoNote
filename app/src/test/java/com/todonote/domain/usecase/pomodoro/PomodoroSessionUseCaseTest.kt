package com.todonote.domain.usecase.pomodoro

import com.todonote.domain.model.PomodoroSession
import com.todonote.domain.model.SessionType
import com.todonote.domain.repository.IPomodoroRepository
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PomodoroSessionUseCaseTest {

    private lateinit var repository: IPomodoroRepository

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
    }

    @Test
    fun `getTodaySessions returns flow from repository`() = runTest {
        val sessions = listOf(
            PomodoroSession(id = 1, duration = 25 * 60, type = SessionType.FOCUS)
        )
        every { repository.getTodaySessions() } returns flowOf(sessions)

        val useCase = PomodoroSessionUseCase(repository)
        val result = useCase.getTodaySessions().first()

        assertEquals(sessions, result)
    }

    @Test
    fun `getAllSessions returns flow from repository`() = runTest {
        val sessions = listOf(
            PomodoroSession(id = 1, duration = 25 * 60, type = SessionType.FOCUS),
            PomodoroSession(id = 2, duration = 5 * 60, type = SessionType.BREAK)
        )
        every { repository.getAll() } returns flowOf(sessions)

        val useCase = PomodoroSessionUseCase(repository)
        val result = useCase.getAllSessions().first()

        assertEquals(sessions, result)
    }

    @Test
    fun `startSession creates session with correct type and duration`() = runTest {
        val slot = slot<PomodoroSession>()
        coEvery { repository.insert(capture(slot)) } returns 42L

        val useCase = PomodoroSessionUseCase(repository)
        val result = useCase.startSession(SessionType.FOCUS, 25 * 60, taskId = 5L)

        assertEquals(42L, result)
        assertEquals(SessionType.FOCUS, slot.captured.type)
        assertEquals(25 * 60, slot.captured.duration)
        assertEquals(5L, slot.captured.taskId)
        assertFalse(slot.captured.isCompleted)
        assertNotNull(slot.captured.startedAt)
    }

    @Test
    fun `completeSession marks session completed`() = runTest {
        val session = PomodoroSession(id = 1, duration = 25 * 60, type = SessionType.FOCUS)
        coEvery { repository.update(any()) } just Runs

        val useCase = PomodoroSessionUseCase(repository)
        useCase.completeSession(session)

        coVerify { repository.update(any()) }
    }

    @Test
    fun `cancelSession marks session not completed`() = runTest {
        val session = PomodoroSession(id = 1, duration = 25 * 60, type = SessionType.FOCUS)
        val slot = slot<PomodoroSession>()
        coEvery { repository.update(capture(slot)) } just Runs

        val useCase = PomodoroSessionUseCase(repository)
        useCase.cancelSession(session)

        assertFalse(slot.captured.isCompleted)
        assertNotNull(slot.captured.endedAt)
    }
}
