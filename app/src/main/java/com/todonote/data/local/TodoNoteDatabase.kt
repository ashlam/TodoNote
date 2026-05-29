package com.todonote.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.todonote.data.local.converter.Converters
import com.todonote.data.local.dao.*
import com.todonote.data.local.entity.*

@Database(
    entities = [
        ListEntity::class,
        TaskEntity::class,
        TagEntity::class,
        TaskTagCrossRef::class,
        HabitEntity::class,
        HabitCheckInEntity::class,
        PomodoroSessionEntity::class,
        AchievementEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TodoNoteDatabase : RoomDatabase() {
    abstract fun listDao(): ListDao
    abstract fun taskDao(): TaskDao
    abstract fun tagDao(): TagDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCheckInDao(): HabitCheckInDao
    abstract fun pomodoroSessionDao(): PomodoroSessionDao
    abstract fun achievementDao(): AchievementDao
    abstract fun taskTagCrossRefDao(): TaskTagCrossRefDao
}
