package com.todonote.data.local.dao

import androidx.room.*
import com.todonote.data.local.entity.ListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {
    @Query("SELECT * FROM lists ORDER BY sortOrder ASC, createdAt DESC")
    fun getAll(): Flow<List<ListEntity>>

    @Query("SELECT * FROM lists ORDER BY sortOrder ASC, createdAt DESC")
    suspend fun getAllSync(): List<ListEntity>

    @Query("SELECT * FROM lists WHERE id = :id")
    suspend fun getById(id: Long): ListEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ListEntity): Long

    @Update
    suspend fun update(list: ListEntity)

    @Delete
    suspend fun delete(list: ListEntity)

    @Query("SELECT MAX(sortOrder) FROM lists")
    suspend fun getMaxSortOrder(): Int?

    @Query("UPDATE lists SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("DELETE FROM lists")
    suspend fun deleteAll()
}
