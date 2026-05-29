package com.todonote.presentation.habits

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todonote.data.local.dao.HabitCheckInDao
import com.todonote.data.local.entity.HabitCheckInEntity
import com.todonote.domain.model.Habit
import com.todonote.domain.repository.IHabitRepository
import com.todonote.domain.usecase.habit.CheckInHabitUseCase
import com.todonote.domain.usecase.habit.CalculateStreakUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class HabitDetailUiState(
    val habit: Habit? = null,
    val name: String = "",
    val description: String = "",
    val color: Int = 0xFF2196F3.toInt(),
    val checkIns: List<HabitCheckInEntity> = emptyList(),
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

class HabitDetailViewModel(
    private val context: Context,
    private val habitRepository: IHabitRepository,
    private val habitCheckInDao: HabitCheckInDao,
    private val checkInHabitUseCase: CheckInHabitUseCase,
    private val calculateStreakUseCase: CalculateStreakUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitDetailUiState())
    val uiState: StateFlow<HabitDetailUiState> = _uiState.asStateFlow()

    fun loadHabit(habitId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            if (habitId == 0L) {
                _uiState.update {
                    it.copy(
                        habit = null,
                        name = "",
                        description = "",
                        color = 0xFF2196F3.toInt(),
                        checkIns = emptyList(),
                        currentStreak = 0,
                        bestStreak = 0,
                        isLoading = false
                    )
                }
            } else {
                try {
                    val habit = habitRepository.getById(habitId)
                    if (habit != null) {
                        habitCheckInDao.getByHabitId(habitId)
                            .catch { e ->
                                _uiState.update { it.copy(error = e.message, isLoading = false) }
                            }
                            .collect { checkIns ->
                                val (currentStreak, bestStreak) = calculateStreakUseCase(checkIns)
                                _uiState.update { state ->
                                    state.copy(
                                        habit = habit,
                                        name = habit.name,
                                        description = habit.description,
                                        color = habit.color,
                                        checkIns = checkIns,
                                        currentStreak = currentStreak,
                                        bestStreak = bestStreak,
                                        isLoading = false
                                    )
                                }
                            }
                    } else {
                        _uiState.update { it.copy(error = "习惯不存在", isLoading = false) }
                    }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = e.message, isLoading = false) }
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun updateColor(color: Int) {
        _uiState.update { it.copy(color = color) }
    }

    fun saveHabit() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.name.isBlank()) return@launch

            val habit = state.habit?.copy(
                name = state.name,
                description = state.description,
                color = state.color,
                updatedAt = System.currentTimeMillis()
            ) ?: Habit(
                name = state.name,
                description = state.description,
                color = state.color
            )

            if (habit.id == 0L) {
                habitRepository.insert(habit)
            } else {
                habitRepository.update(habit)
            }

            _uiState.update { it.copy(isSaved = true) }
        }
    }

    fun deleteHabit() {
        viewModelScope.launch {
            _uiState.value.habit?.let {
                habitRepository.delete(it)
            }
        }
    }

    fun archiveHabit() {
        viewModelScope.launch {
            _uiState.value.habit?.let {
                habitRepository.update(it.copy(isArchived = true))
            }
        }
    }

    fun unarchiveHabit() {
        viewModelScope.launch {
            _uiState.value.habit?.let {
                habitRepository.update(it.copy(isArchived = false))
            }
        }
    }

    fun checkIn(date: Long, isComplete: Boolean = true) {
        viewModelScope.launch {
            val habitId = _uiState.value.habit?.id ?: return@launch
            checkInHabitUseCase(habitId, date, isComplete)
        }
    }
}
