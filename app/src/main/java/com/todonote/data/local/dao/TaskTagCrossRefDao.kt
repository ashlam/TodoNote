package com.todonote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.todonote.data.local.entity.TaskTagCrossRef

@Dao
interface TaskTagCrossRefDao {
    @Query("SELECT * FROM task_tag_cross_ref")
    suspend fun getAllSync(): List<TaskTagCrossRef>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(crossRef: TaskTagCrossRef)

    @Query("DELETE FROM task_tag_cross_ref")
    suspend fun deleteAll()
}
