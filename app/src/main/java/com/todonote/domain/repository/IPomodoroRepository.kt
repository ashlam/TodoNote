package com.todonote.domain.repository

import com.todonote.domain.model.PomodoroSession
import kotlinx.coroutines.flow.Flow

interface IPomodoroRepository {
    fun getAll(): Flow<List<PomodoroSession>>
    fun getByTaskId(taskId: Long): Flow<List<PomodoroSession>>
    fun getTodaySessions(): Flow<List<PomodoroSession>>
    suspend fun insert(session: PomodoroSession): Long
    suspend fun update(session: PomodoroSession)
    suspend fun delete(session: PomodoroSession)
}
