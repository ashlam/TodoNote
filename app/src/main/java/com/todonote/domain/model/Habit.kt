package com.todonote.domain.model

data class Habit(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val frequency: Frequency = Frequency.DAILY,
    val targetCount: Int = 1,
    val color: Int,
    val icon: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0
)

enum class Frequency(val value: Int) {
    DAILY(0),
    WEEKLY(1);

    companion object {
        fun fromValue(value: Int): Frequency = entries.find { it.value == value } ?: DAILY
    }
}
