package com.todonote.domain.model

data class HabitCheckIn(
    val id: Long = 0,
    val habitId: Long,
    val date: Long,
    val isComplete: Boolean = false,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
