package com.todonote.domain.model

data class PomodoroSession(
    val id: Long = 0,
    val taskId: Long? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null,
    val duration: Int = 25 * 60,
    val type: SessionType = SessionType.FOCUS,
    val isCompleted: Boolean = false
)

enum class SessionType(val value: Int) {
    FOCUS(0),
    BREAK(1),
    LONG_BREAK(2);

    companion object {
        fun fromValue(value: Int): SessionType = entries.find { it.value == value } ?: FOCUS
    }
}
