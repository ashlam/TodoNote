package com.todonote.presentation.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todonote.domain.model.Habit
import com.todonote.domain.usecase.habit.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class HabitListUiState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class HabitListViewModel(
    private val getHabitsUseCase: GetHabitsUseCase,
    private val createHabitUseCase: CreateHabitUseCase,
    private val deleteHabitUseCase: DeleteHabitUseCase,
    private val checkInHabitUseCase: CheckInHabitUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HabitListUiState())
    val uiState: StateFlow<HabitListUiState> = _uiState.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            getHabitsUseCase()
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { habits ->
                    _uiState.update { it.copy(habits = habits, isLoading = false) }
                }
        }
    }

    fun checkInHabit(habitId: Long) {
        viewModelScope.launch {
            val today = System.currentTimeMillis()
            val normalizedToday = today - (today % TimeUnit.DAYS.toMillis(1))
            checkInHabitUseCase(habitId, normalizedToday)
        }
    }

    fun createHabit(name: String, description: String, color: Int) {
        viewModelScope.launch {
            val habit = Habit(
                name = name,
                description = description,
                color = color
            )
            createHabitUseCase(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            deleteHabitUseCase(habit)
        }
    }
}
