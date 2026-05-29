package com.todonote.domain.repository

import com.todonote.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface ITaskRepository {
    fun getByListId(listId: Long): Flow<List<Task>>
    suspend fun getById(id: Long): Task?
    fun getIncomplete(): Flow<List<Task>>
    fun getCompleted(): Flow<List<Task>>
    fun search(query: String): Flow<List<Task>>
    fun getByDateRange(start: Long, end: Long): Flow<List<Task>>
    suspend fun insert(task: Task): Long
    suspend fun update(task: Task)
    suspend fun delete(task: Task)
    suspend fun toggleCompleted(id: Long)
    suspend fun reorder(listId: Long, fromIndex: Int, toIndex: Int)
    suspend fun updateSortOrder(id: Long, sortOrder: Int)
}
