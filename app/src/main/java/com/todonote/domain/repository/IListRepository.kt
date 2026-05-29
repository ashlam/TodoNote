package com.todonote.domain.repository

import com.todonote.domain.model.TaskList
import kotlinx.coroutines.flow.Flow

interface IListRepository {
    fun getAll(): Flow<List<TaskList>>
    suspend fun getById(id: Long): TaskList?
    suspend fun insert(list: TaskList): Long
    suspend fun update(list: TaskList)
    suspend fun delete(list: TaskList)
    suspend fun reorder(fromIndex: Int, toIndex: Int)
}
