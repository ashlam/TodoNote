package com.todonote.data.repository

import com.todonote.data.local.dao.ListDao
import com.todonote.data.local.entity.ListEntity
import com.todonote.domain.model.TaskList
import com.todonote.domain.repository.IListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ListRepository(private val listDao: ListDao) : IListRepository {

    override fun getAll(): Flow<List<TaskList>> = listDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getById(id: Long): TaskList? = listDao.getById(id)?.toDomain()

    override suspend fun insert(list: TaskList): Long {
        val maxOrder = listDao.getMaxSortOrder() ?: -1
        return listDao.insert(list.toEntity(sortOrder = maxOrder + 1))
    }

    override suspend fun update(list: TaskList) = listDao.update(list.toEntity())

    override suspend fun delete(list: TaskList) = listDao.delete(list.toEntity())

    override suspend fun reorder(fromIndex: Int, toIndex: Int) {
        val lists = listDao.getAll().map { it.map { e -> e.toDomain() } }
    }

    private fun ListEntity.toDomain() = TaskList(
        id = id,
        title = title,
        description = description,
        color = color,
        icon = icon,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun TaskList.toEntity(sortOrder: Int = this.sortOrder) = ListEntity(
        id = id,
        title = title,
        description = description,
        color = color,
        icon = icon,
        sortOrder = sortOrder,
        createdAt = createdAt,
        updatedAt = System.currentTimeMillis()
    )
}
