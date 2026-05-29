package com.todonote.data.repository

import com.todonote.data.local.dao.TagDao
import com.todonote.data.local.entity.TagEntity
import com.todonote.domain.model.Tag
import com.todonote.domain.repository.ITagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TagRepository(private val tagDao: TagDao) : ITagRepository {

    override fun getAll(): Flow<List<Tag>> = tagDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    override suspend fun getById(id: Long): Tag? = tagDao.getById(id)?.toDomain()

    override suspend fun insert(tag: Tag): Long = tagDao.insert(tag.toEntity())

    override suspend fun update(tag: Tag) = tagDao.update(tag.toEntity())

    override suspend fun delete(tag: Tag) = tagDao.delete(tag.toEntity())

    private fun TagEntity.toDomain() = Tag(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt
    )

    private fun Tag.toEntity() = TagEntity(
        id = id,
        name = name,
        color = color,
        createdAt = createdAt
    )
}
