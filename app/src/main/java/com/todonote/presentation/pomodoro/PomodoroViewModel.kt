package com.todonote.presentation.pomodoro

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.todonote.domain.model.PomodoroSession
import com.todonote.domain.model.SessionType
import com.todonote.domain.usecase.pomodoro.PomodoroSessionUseCase
import com.todonote.presentation.settings.dataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

sealed class TimerState {
    data object Idle : TimerState()
    data object Running : TimerState()
    data object Paused : TimerState()
}

data class PomodoroUiState(
    val timerState: TimerState = TimerState.Idle,
    val sessionType: SessionType = SessionType.FOCUS,
    val timeRemaining: Int = 25 * 60,
    val totalDuration: Int = 25 * 60,
    val completedSessions: Int = 0,
    val todaySessionCount: Int = 0,
    val todayFocusMinutes: Int = 0,
    val recentSessions: List<PomodoroSession> = emptyList()
)

class PomodoroViewModel(
    private val context: Context,
    private val pomodoroSessionUseCase: PomodoroSessionUseCase
) : ViewModel() {

    companion object {
        const val DEFAULT_FOCUS_MINUTES = 25
        const val DEFAULT_BREAK_MINUTES = 5
        const val DEFAULT_LONG_BREAK_MINUTES = 15
        const val SESSIONS_BEFORE_LONG_BREAK = 4
    }

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var currentSessionId: Long? = null
    private var configuredFocusMinutes = DEFAULT_FOCUS_MINUTES
    private var configuredBreakMinutes = DEFAULT_BREAK_MINUTES
    private var configuredLongBreakMinutes = DEFAULT_LONG_BREAK_MINUTES

    init {
        viewModelScope.launch {
            context.dataStore.data.collect { prefs ->
                val focus = prefs[intPreferencesKey("focus_minutes")] ?: DEFAULT_FOCUS_MINUTES
                val breakMin = prefs[intPreferencesKey("break_minutes")] ?: DEFAULT_BREAK_MINUTES
                val longBreak = prefs[intPreferencesKey("long_break_minutes")] ?: DEFAULT_LONG_BREAK_MINUTES

                configuredFocusMinutes = focus
                configuredBreakMinutes = breakMin
                configuredLongBreakMinutes = longBreak

                val currentType = _uiState.value.sessionType
                val currentDuration = when (currentType) {
                    SessionType.FOCUS -> focus * 60
                    SessionType.BREAK -> breakMin * 60
                    SessionType.LONG_BREAK -> longBreak * 60
                }

                _uiState.update {
                    it.copy(
                        totalDuration = currentDuration,
                        timeRemaining = if (it.timerState == TimerState.Idle) currentDuration else it.timeRemaining
                    )
                }
            }
        }

        viewModelScope.launch {
            pomodoroSessionUseCase.getTodaySessions().collect { sessions ->
                val completed = sessions.count { it.isCompleted && it.type == SessionType.FOCUS }
                val focusMinutes = sessions
                    .filter { it.isCompleted && it.type == SessionType.FOCUS }
                    .sumOf { it.duration } / 60
                _uiState.update {
                    it.copy(
                        todaySessionCount = completed,
                        todayFocusMinutes = focusMinutes
                    )
                }
            }
        }

        viewModelScope.launch {
            pomodoroSessionUseCase.getAllSessions().collect { sessions ->
                _uiState.update {
                    it.copy(recentSessions = sessions.take(20))
                }
            }
        }
    }

    fun startTimer() {
        if (_uiState.value.timerState == TimerState.Running) return

        val state = _uiState.value
        if (state.timerState == TimerState.Idle) {
            viewModelScope.launch {
                val sessionId = pomodoroSessionUseCase.startSession(
                    type = state.sessionType,
                    duration = state.totalDuration
                )
                currentSessionId = sessionId
            }
        }

        _uiState.update { it.copy(timerState = TimerState.Running) }

        timerJob = viewModelScope.launch {
            while (isActive && _uiState.value.timeRemaining > 0) {
                delay(1000)
                _uiState.update { current ->
                    val newTime = current.timeRemaining - 1
                    if (newTime <= 0) {
                        current.copy(timeRemaining = 0)
                    } else {
                        current.copy(timeRemaining = newTime)
                    }
                }
                if (_uiState.value.timeRemaining <= 0) {
                    onTimerComplete()
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(timerState = TimerState.Paused) }
    }

    fun resetTimer() {
        timerJob?.cancel()
        val state = _uiState.value
        if (currentSessionId != null && state.timerState != TimerState.Idle) {
            viewModelScope.launch {
                val session = PomodoroSession(
                    id = currentSessionId ?: 0,
                    type = state.sessionType,
                    duration = state.totalDuration,
                    startedAt = System.currentTimeMillis() - (state.totalDuration - state.timeRemaining) * 1000L,
                    isCompleted = false
                )
                pomodoroSessionUseCase.cancelSession(session)
                currentSessionId = null
            }
        }
        _uiState.update {
            it.copy(
                timerState = TimerState.Idle,
                timeRemaining = it.totalDuration
            )
        }
    }

    private fun onTimerComplete() {
        timerJob?.cancel()
        val state = _uiState.value

        viewModelScope.launch {
            currentSessionId?.let { id ->
                val session = PomodoroSession(
                    id = id,
                    type = state.sessionType,
                    duration = state.totalDuration,
                    startedAt = System.currentTimeMillis() - state.totalDuration * 1000L,
                    isCompleted = true
                )
                pomodoroSessionUseCase.completeSession(session)
            }

            val newCompletedSessions = if (state.sessionType == SessionType.FOCUS) {
                state.completedSessions + 1
            } else {
                state.completedSessions
            }

            val nextType = when {
                state.sessionType == SessionType.FOCUS && newCompletedSessions % SESSIONS_BEFORE_LONG_BREAK == 0 ->
                    SessionType.LONG_BREAK
                state.sessionType == SessionType.FOCUS ->
                    SessionType.BREAK
                else ->
                    SessionType.FOCUS
            }

            val nextDuration = when (nextType) {
                SessionType.FOCUS -> configuredFocusMinutes * 60
                SessionType.BREAK -> configuredBreakMinutes * 60
                SessionType.LONG_BREAK -> configuredLongBreakMinutes * 60
            }

            _uiState.update {
                it.copy(
                    timerState = TimerState.Idle,
                    sessionType = nextType,
                    timeRemaining = nextDuration,
                    totalDuration = nextDuration,
                    completedSessions = newCompletedSessions
                )
            }
            currentSessionId = null
        }
    }

    fun skipSession() {
        timerJob?.cancel()
        val state = _uiState.value

        viewModelScope.launch {
            currentSessionId?.let { id ->
                val session = PomodoroSession(
                    id = id,
                    type = state.sessionType,
                    duration = state.totalDuration,
                    startedAt = System.currentTimeMillis() - (state.totalDuration - state.timeRemaining) * 1000L,
                    isCompleted = false
                )
                pomodoroSessionUseCase.cancelSession(session)
            }

            val newCompletedSessions = if (state.sessionType == SessionType.FOCUS) {
                state.completedSessions + 1
            } else {
                state.completedSessions
            }

            val nextType = when {
                state.sessionType == SessionType.FOCUS && newCompletedSessions % SESSIONS_BEFORE_LONG_BREAK == 0 ->
                    SessionType.LONG_BREAK
                state.sessionType == SessionType.FOCUS ->
                    SessionType.BREAK
                else ->
                    SessionType.FOCUS
            }

            val nextDuration = when (nextType) {
                SessionType.FOCUS -> configuredFocusMinutes * 60
                SessionType.BREAK -> configuredBreakMinutes * 60
                SessionType.LONG_BREAK -> configuredLongBreakMinutes * 60
            }

            _uiState.update {
                it.copy(
                    timerState = TimerState.Idle,
                    sessionType = nextType,
                    timeRemaining = nextDuration,
                    totalDuration = nextDuration,
                    completedSessions = newCompletedSessions
                )
            }
            currentSessionId = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
