package com.todonote.data.local.dao

import androidx.room.*
import com.todonote.data.local.entity.HabitCheckInEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCheckInDao {
    @Query("SELECT * FROM habit_check_ins WHERE habitId = :habitId ORDER BY date DESC")
    fun getByHabitId(habitId: Long): Flow<List<HabitCheckInEntity>>

    @Query("SELECT * FROM habit_check_ins ORDER BY date DESC")
    suspend fun getAllSync(): List<HabitCheckInEntity>

    @Query("SELECT * FROM habit_check_ins WHERE habitId = :habitId AND date = :date")
    suspend fun getByHabitAndDate(habitId: Long, date: Long): HabitCheckInEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(checkIn: HabitCheckInEntity): Long

    @Update
    suspend fun update(checkIn: HabitCheckInEntity)

    @Delete
    suspend fun delete(checkIn: HabitCheckInEntity)

    @Query("DELETE FROM habit_check_ins")
    suspend fun deleteAll()
}
