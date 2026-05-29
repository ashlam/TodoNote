package com.todonote.di

import com.todonote.presentation.calendar.CalendarViewModel
import com.todonote.presentation.habits.HabitListViewModel
import com.todonote.presentation.habits.HabitDetailViewModel
import com.todonote.presentation.pomodoro.PomodoroViewModel
import com.todonote.presentation.settings.SettingsViewModel
import com.todonote.presentation.settings.TagManagementViewModel
import com.todonote.presentation.statistics.StatisticsViewModel
import com.todonote.presentation.tasks.TaskDetailViewModel
import com.todonote.presentation.tasks.TaskListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel {
        TaskListViewModel(
            getListsUseCase = get(),
            createListUseCase = get(),
            getTasksUseCase = get(),
            createTaskUseCase = get(),
            toggleTaskUseCase = get(),
            deleteTaskUseCase = get(),
            searchTasksUseCase = get(),
            taskRepository = get()
        )
    }
    viewModel {
        TaskDetailViewModel(
            context = get(),
            getTasksUseCase = get(),
            createTaskUseCase = get(),
            updateTaskUseCase = get(),
            deleteTaskUseCase = get()
        )
    }
    viewModel {
        CalendarViewModel(
            taskRepository = get(),
            createTaskUseCase = get()
        )
    }
    viewModel {
        SettingsViewModel(context = get(), backupManager = get())
    }
    viewModel {
        HabitListViewModel(
            getHabitsUseCase = get(),
            createHabitUseCase = get(),
            deleteHabitUseCase = get(),
            checkInHabitUseCase = get()
        )
    }
    viewModel {
        HabitDetailViewModel(
            context = get(),
            habitRepository = get(),
            habitCheckInDao = get(),
            checkInHabitUseCase = get(),
            calculateStreakUseCase = get()
        )
    }
    viewModel {
        PomodoroViewModel(
            context = get(),
            pomodoroSessionUseCase = get()
        )
    }
    viewModel {
        StatisticsViewModel(
            taskRepository = get(),
            habitRepository = get(),
            pomodoroRepository = get(),
            achievementRepository = get(),
            unlockAchievementUseCase = get()
        )
    }
    viewModel {
        TagManagementViewModel(tagRepository = get())
    }
}
