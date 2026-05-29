package com.todonote.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["listId"]),
        Index(value = ["isCompleted"]),
        Index(value = ["dueDate"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val listId: Long,
    val title: String,
    val notes: String = "",
    val dueDate: Long? = null,
    val priority: Int = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val sortOrder: Int = 0,
    val reminderTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    object Priority {
        const val HIGH = 3
        const val MEDIUM = 2
        const val LOW = 1
        const val NONE = 0
    }
}
