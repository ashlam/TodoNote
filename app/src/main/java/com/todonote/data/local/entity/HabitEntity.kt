package com.todonote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val frequency: Int = Frequency.DAILY,
    val targetCount: Int = 1,
    val color: Int,
    val icon: String = "",
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
) {
    object Frequency {
        const val DAILY = 0
        const val WEEKLY = 1
    }
}
