package com.todonote.data.repository

import com.todonote.data.local.dao.PomodoroSessionDao
import com.todonote.data.local.entity.PomodoroSessionEntity
import com.todonote.domain.model.PomodoroSession
import com.todonote.domain.model.SessionType
import com.todonote.domain.repository.IPomodoroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class PomodoroRepository(private val sessionDao: PomodoroSessionDao) : IPomodoroRepository {

    override fun getAll(): Flow<List<PomodoroSession>> = sessionDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun getByTaskId(taskId: Long): Flow<List<PomodoroSession>> = sessionDao.getByTaskId(taskId).map { entities ->
        entities.map { it.toDomain() }
    }

    override fun getTodaySessions(): Flow<List<PomodoroSession>> {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return sessionDao.getFromDate(cal.timeInMillis).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insert(session: PomodoroSession): Long = sessionDao.insert(session.toEntity())

    override suspend fun update(session: PomodoroSession) = sessionDao.update(session.toEntity())

    override suspend fun delete(session: PomodoroSession) = sessionDao.delete(session.toEntity())

    private fun PomodoroSessionEntity.toDomain() = PomodoroSession(
        id = id,
        taskId = taskId,
        startedAt = startedAt,
        endedAt = endedAt,
        duration = duration,
        type = SessionType.fromValue(type),
        isCompleted = isCompleted
    )

    private fun PomodoroSession.toEntity() = PomodoroSessionEntity(
        id = id,
        taskId = taskId,
        startedAt = startedAt,
        endedAt = endedAt,
        duration = duration,
        type = type.value,
        isCompleted = isCompleted
    )
}
