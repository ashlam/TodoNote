package com.todonote.data.repository

import com.todonote.data.local.dao.TaskDao
import com.todonote.data.local.entity.TaskEntity
import com.todonote.domain.model.Priority
import com.todonote.domain.model.Task
import com.todonote.domain.repository.ITaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TaskRepository(private val taskDao: TaskDao) : ITaskRepository {

    override fun getByListId(listId: Long): Flow<List<Task>> = taskDao.getByListId(listId).map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getById(id: Long): Task? = taskDao.getById(id)?.toDomain()

    override fun getIncomplete(): Flow<List<Task>> = taskDao.getIncomplete().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun getCompleted(): Flow<List<Task>> = taskDao.getCompleted().map { entities ->
        entities.map { it.toDomain() }
    }

    override fun search(query: String): Flow<List<Task>> = taskDao.search(query).map { entities ->
        entities.map { it.toDomain() }
    }

    override fun getByDateRange(start: Long, end: Long): Flow<List<Task>> =
        taskDao.getByDateRange(start, end).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun insert(task: Task): Long {
        val maxOrder = taskDao.getMaxSortOrder(task.listId) ?: -1
        return taskDao.insert(task.toEntity(sortOrder = maxOrder + 1))
    }

    override suspend fun update(task: Task) = taskDao.update(task.toEntity())

    override suspend fun delete(task: Task) = taskDao.delete(task.toEntity())

    override suspend fun toggleCompleted(id: Long) {
        val task = taskDao.getById(id) ?: return
        val newCompleted = !task.isCompleted
        val completedAt = if (newCompleted) System.currentTimeMillis() else null
        taskDao.setCompleted(id, newCompleted, completedAt)
    }

    override suspend fun reorder(listId: Long, fromIndex: Int, toIndex: Int) {
    }

    override suspend fun updateSortOrder(id: Long, sortOrder: Int) {
        taskDao.updateSortOrder(id, sortOrder)
    }

    private fun TaskEntity.toDomain() = Task(
        id = id,
        listId = listId,
        title = title,
        notes = notes,
        dueDate = dueDate,
        priority = Priority.fromValue(priority),
        isCompleted = isCompleted,
        completedAt = completedAt,
        sortOrder = sortOrder,
        reminderTime = reminderTime,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Task.toEntity(sortOrder: Int = this.sortOrder) = TaskEntity(
        id = id,
        listId = listId,
        title = title,
        notes = notes,
        dueDate = dueDate,
        priority = priority.value,
        isCompleted = isCompleted,
        completedAt = completedAt,
        sortOrder = sortOrder,
        reminderTime = reminderTime,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}
