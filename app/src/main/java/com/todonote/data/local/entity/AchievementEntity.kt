package com.todonote.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconRes: String = "",
    val conditionType: String,
    val conditionValue: Int = 0,
    val unlockedAt: Long? = null
)
