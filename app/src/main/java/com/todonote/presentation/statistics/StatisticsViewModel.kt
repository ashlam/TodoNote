package com.todonote.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todonote.domain.model.Achievement
import com.todonote.domain.model.SessionType
import com.todonote.domain.repository.IAchievementRepository
import com.todonote.domain.repository.IHabitRepository
import com.todonote.domain.repository.IPomodoroRepository
import com.todonote.domain.repository.ITaskRepository
import com.todonote.domain.usecase.achievement.UnlockAchievementUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class StatisticsUiState(
    val todayCompletedTasks: Int = 0,
    val todayTotalTasks: Int = 0,
    val weekCompletedTasks: Int = 0,
    val weekTotalTasks: Int = 0,
    val monthCompletedTasks: Int = 0,
    val monthTotalTasks: Int = 0,
    val todayFocusMinutes: Int = 0,
    val todayPomodoroSessions: Int = 0,
    val totalFocusMinutes: Int = 0,
    val totalPomodoroSessions: Int = 0,
    val habitStreaks: List<Pair<String, Int>> = emptyList(),
    val achievements: List<Achievement> = emptyList(),
    val unlockedCount: Int = 0,
    val totalAchievementCount: Int = 0,
    val newlyUnlocked: List<Achievement> = emptyList(),
    val isLoading: Boolean = false
)

class StatisticsViewModel(
    private val taskRepository: ITaskRepository,
    private val habitRepository: IHabitRepository,
    private val pomodoroRepository: IPomodoroRepository,
    private val achievementRepository: IAchievementRepository,
    private val unlockAchievementUseCase: UnlockAchievementUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    init {
        seedAchievements()
        loadStatistics()
    }

    private fun seedAchievements() {
        viewModelScope.launch {
            achievementRepository.getAll().first().let { existing ->
                if (existing.isEmpty()) {
                    defaultAchievements.forEach { achievementRepository.insert(it) }
                }
            }
        }
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val weekStart = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -7)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val monthStart = Calendar.getInstance().apply {
                add(Calendar.MONTH, -1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Tasks
            launch {
                taskRepository.getByDateRange(todayStart, todayStart + 86400000).collect { tasks ->
                    val completed = tasks.count { it.isCompleted }
                    _uiState.update { it.copy(todayCompletedTasks = completed, todayTotalTasks = tasks.size) }
                }
            }

            launch {
                taskRepository.getByDateRange(weekStart, System.currentTimeMillis()).collect { tasks ->
                    val completed = tasks.count { it.isCompleted }
                    _uiState.update { it.copy(weekCompletedTasks = completed, weekTotalTasks = tasks.size) }
                }
            }

            launch {
                taskRepository.getByDateRange(monthStart, System.currentTimeMillis()).collect { tasks ->
                    val completed = tasks.count { it.isCompleted }
                    _uiState.update { it.copy(monthCompletedTasks = completed, monthTotalTasks = tasks.size) }
                }
            }

            // Pomodoro
            launch {
                pomodoroRepository.getTodaySessions().collect { sessions ->
                    val completed = sessions.filter { it.isCompleted && it.type == SessionType.FOCUS }
                    _uiState.update {
                        it.copy(
                            todayPomodoroSessions = completed.size,
                            todayFocusMinutes = completed.sumOf { s -> s.duration } / 60
                        )
                    }
                }
            }

            launch {
                pomodoroRepository.getAll().collect { sessions ->
                    val completed = sessions.filter { it.isCompleted && it.type == SessionType.FOCUS }
                    _uiState.update {
                        it.copy(
                            totalPomodoroSessions = completed.size,
                            totalFocusMinutes = completed.sumOf { s -> s.duration } / 60
                        )
                    }
                }
            }

            // Habits
            launch {
                habitRepository.getActive().collect { habits ->
                    val streaks = habits.map { it.name to it.currentStreak }
                    _uiState.update { it.copy(habitStreaks = streaks) }
                }
            }

            // Achievements
            launch {
                achievementRepository.getAll().collect { achievements ->
                    val unlocked = achievements.count { it.isUnlocked }
                    _uiState.update {
                        it.copy(
                            achievements = achievements,
                            unlockedCount = unlocked,
                            totalAchievementCount = achievements.size,
                            isLoading = false
                        )
                    }
                }
            }
        }
    }

    fun checkAndUnlockAchievements() {
        viewModelScope.launch {
            val state = _uiState.value
            val stats = UnlockAchievementUseCase.AchievementStats(
                todayCompletedTasks = state.todayCompletedTasks,
                todayPomodoroSessions = state.todayPomodoroSessions,
                totalFocusMinutes = state.totalFocusMinutes,
                habitStreaks = state.habitStreaks
            )

            val result = unlockAchievementUseCase(stats, state.achievements)
            if (result.newlyUnlocked.isNotEmpty()) {
                _uiState.update { it.copy(newlyUnlocked = result.newlyUnlocked) }
            }
        }
    }

    fun clearNewlyUnlocked() {
        _uiState.update { it.copy(newlyUnlocked = emptyList()) }
    }

    companion object {
        private val defaultAchievements = listOf(
            Achievement(
                name = "初次完成",
                description = "完成第一个任务",
                conditionType = "TASKS_COMPLETED",
                conditionValue = 1
            ),
            Achievement(
                name = "任务达人",
                description = "一天内完成 5 个任务",
                conditionType = "TASKS_COMPLETED",
                conditionValue = 5
            ),
            Achievement(
                name = "专注开始",
                description = "完成 1 次番茄钟",
                conditionType = "POMODORO_SESSIONS",
                conditionValue = 1
            ),
            Achievement(
                name = "专注大师",
                description = "一天内完成 4 次番茄钟",
                conditionType = "POMODORO_SESSIONS",
                conditionValue = 4
            ),
            Achievement(
                name = "习惯养成",
                description = "连续打卡 3 天",
                conditionType = "STREAK_DAYS",
                conditionValue = 3
            ),
            Achievement(
                name = "坚持不懈",
                description = "连续打卡 7 天",
                conditionType = "STREAK_DAYS",
                conditionValue = 7
            ),
            Achievement(
                name = "深度工作",
                description = "累计专注 60 分钟",
                conditionType = "TOTAL_FOCUS_MINUTES",
                conditionValue = 60
            ),
            Achievement(
                name = "时间管理大师",
                description = "累计专注 300 分钟",
                conditionType = "TOTAL_FOCUS_MINUTES",
                conditionValue = 300
            )
        )
    }
}
