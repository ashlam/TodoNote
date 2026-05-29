package com.todonote.domain.repository

import com.todonote.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface ITagRepository {
    fun getAll(): Flow<List<Tag>>
    suspend fun getById(id: Long): Tag?
    suspend fun insert(tag: Tag): Long
    suspend fun update(tag: Tag)
    suspend fun delete(tag: Tag)
}
