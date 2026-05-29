package com.todonote.domain.usecase.achievement

import com.todonote.domain.model.Achievement
import com.todonote.domain.repository.IAchievementRepository

class UnlockAchievementUseCase(
    private val achievementRepository: IAchievementRepository
) {

    data class AchievementStats(
        val todayCompletedTasks: Int = 0,
        val weekCompletedTasks: Int = 0,
        val todayPomodoroSessions: Int = 0,
        val totalFocusMinutes: Int = 0,
        val habitStreaks: List<Pair<String, Int>> = emptyList()
    )

    data class UnlockResult(
        val newlyUnlocked: List<Achievement> = emptyList()
    )

    suspend operator fun invoke(stats: AchievementStats, existingAchievements: List<Achievement>): UnlockResult {
        val newlyUnlocked = mutableListOf<Achievement>()

        existingAchievements.forEach { achievement ->
            if (achievement.isUnlocked) return@forEach

            val shouldUnlock = when (achievement.conditionType) {
                "TASKS_COMPLETED" -> stats.todayCompletedTasks >= achievement.conditionValue
                "STREAK_DAYS" -> stats.habitStreaks.any { it.second >= achievement.conditionValue }
                "POMODORO_SESSIONS" -> stats.todayPomodoroSessions >= achievement.conditionValue
                "TOTAL_FOCUS_MINUTES" -> stats.totalFocusMinutes >= achievement.conditionValue
                else -> false
            }

            if (shouldUnlock) {
                achievementRepository.unlock(achievement.id)
                newlyUnlocked.add(achievement.copy(isUnlocked = true, unlockedAt = System.currentTimeMillis()))
            }
        }

        return UnlockResult(newlyUnlocked)
    }
}
