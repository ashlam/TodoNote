package com.todonote.domain.model

data class Achievement(
    val id: Long = 0,
    val name: String,
    val description: String,
    val iconRes: String = "",
    val conditionType: String,
    val conditionValue: Int = 0,
    val unlockedAt: Long? = null,
    val isUnlocked: Boolean = false
)
