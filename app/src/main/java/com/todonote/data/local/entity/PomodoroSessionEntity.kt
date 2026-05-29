package com.todonote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pomodoro_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["taskId"]),
        Index(value = ["startedAt"])
    ]
)
data class PomodoroSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val duration: Int = 25 * 60,
    val type: Int = Type.FOCUS,
    val isCompleted: Boolean = false
) {
    object Type {
        const val FOCUS = 0
        const val BREAK = 1
        const val LONG_BREAK = 2
    }
}
