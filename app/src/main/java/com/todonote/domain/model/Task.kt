package com.todonote.domain.model

data class Task(
    val id: Long = 0,
    val listId: Long,
    val title: String,
    val notes: String = "",
    val dueDate: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val sortOrder: Int = 0,
    val reminderTime: Long? = null,
    val tags: List<Tag> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class Priority(val value: Int) {
    NONE(0),
    LOW(1),
    MEDIUM(2),
    HIGH(3);

    companion object {
        fun fromValue(value: Int): Priority = entries.find { it.value == value } ?: MEDIUM
    }
}
