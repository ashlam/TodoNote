package com.todonote.presentation.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todonote.domain.model.Task
import com.todonote.domain.repository.ITaskRepository
import com.todonote.domain.usecase.task.CreateTaskUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

data class CalendarUiState(
    val currentMonth: Calendar = Calendar.getInstance(),
    val selectedDate: Calendar? = null,
    val tasksByDate: Map<Int, List<Task>> = emptyMap(),
    val selectedDateTasks: List<Task> = emptyList(),
    val isLoading: Boolean = false
)

class CalendarViewModel(
    private val taskRepository: ITaskRepository,
    private val createTaskUseCase: CreateTaskUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadTasksForMonth(_uiState.value.currentMonth)
    }

    fun selectDate(date: Calendar) {
        val sameDay = _uiState.value.selectedDate?.let {
            it.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
            it.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
        } ?: false

        if (sameDay) {
            _uiState.update { it.copy(selectedDate = null, selectedDateTasks = emptyList()) }
        } else {
            val dayTasks = _uiState.value.tasksByDate[date.get(Calendar.DAY_OF_MONTH)] ?: emptyList()
            _uiState.update { it.copy(selectedDate = date, selectedDateTasks = dayTasks) }
        }
    }

    fun previousMonth() {
        val newMonth = _uiState.value.currentMonth.clone() as Calendar
        newMonth.add(Calendar.MONTH, -1)
        _uiState.update { it.copy(currentMonth = newMonth, selectedDate = null, selectedDateTasks = emptyList()) }
        loadTasksForMonth(newMonth)
    }

    fun nextMonth() {
        val newMonth = _uiState.value.currentMonth.clone() as Calendar
        newMonth.add(Calendar.MONTH, 1)
        _uiState.update { it.copy(currentMonth = newMonth, selectedDate = null, selectedDateTasks = emptyList()) }
        loadTasksForMonth(newMonth)
    }

    fun quickCreateTask(title: String, dueDate: Long) {
        viewModelScope.launch {
            val task = Task(
                listId = 1,
                title = title,
                dueDate = dueDate
            )
            createTaskUseCase(task)
        }
    }

    private fun loadTasksForMonth(month: Calendar) {
        viewModelScope.launch {
            val start = month.clone() as Calendar
            start.set(Calendar.DAY_OF_MONTH, 1)
            start.set(Calendar.HOUR_OF_DAY, 0)
            start.set(Calendar.MINUTE, 0)
            start.set(Calendar.SECOND, 0)
            start.set(Calendar.MILLISECOND, 0)

            val end = start.clone() as Calendar
            end.add(Calendar.MONTH, 1)
            end.add(Calendar.MILLISECOND, -1)

            taskRepository.getByDateRange(start.timeInMillis, end.timeInMillis)
                .catch { }
                .collect { tasks ->
                    val grouped = tasks.groupBy { task ->
                        val cal = Calendar.getInstance().apply { timeInMillis = task.dueDate ?: 0 }
                        cal.get(Calendar.DAY_OF_MONTH)
                    }
                    _uiState.update { it.copy(tasksByDate = grouped) }
                }
        }
    }
}
