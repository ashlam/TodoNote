package com.todonote.domain.model

data class TaskList(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val color: Int,
    val icon: String = "",
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val taskCount: Int = 0,
    val completedCount: Int = 0
) {
    val progress: Float
        get() = if (taskCount > 0) completedCount.toFloat() / taskCount else 0f
}
