package com.todonote.domain.usecase.pomodoro

import com.todonote.domain.model.PomodoroSession
import com.todonote.domain.model.SessionType
import com.todonote.domain.repository.IPomodoroRepository

class PomodoroSessionUseCase(private val repository: IPomodoroRepository) {

    fun getTodaySessions() = repository.getTodaySessions()
    fun getAllSessions() = repository.getAll()

    suspend fun startSession(type: SessionType, duration: Int, taskId: Long? = null): Long {
        val session = PomodoroSession(
            taskId = taskId,
            startedAt = System.currentTimeMillis(),
            duration = duration,
            type = type,
            isCompleted = false
        )
        return repository.insert(session)
    }

    suspend fun completeSession(session: PomodoroSession) {
        repository.update(
            session.copy(
                endedAt = System.currentTimeMillis(),
                isCompleted = true
            )
        )
    }

    suspend fun cancelSession(session: PomodoroSession) {
        repository.update(
            session.copy(
                endedAt = System.currentTimeMillis(),
                isCompleted = false
            )
        )
    }
}
