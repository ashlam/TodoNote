package com.todonote.di

import com.todonote.domain.usecase.achievement.UnlockAchievementUseCase
import com.todonote.domain.usecase.habit.*
import com.todonote.domain.usecase.list.*
import com.todonote.domain.usecase.pomodoro.PomodoroSessionUseCase
import com.todonote.domain.usecase.task.*
import org.koin.dsl.module

val useCaseModule = module {
    factory { GetListsUseCase(get()) }
    factory { CreateListUseCase(get()) }
    factory { UpdateListUseCase(get()) }
    factory { DeleteListUseCase(get()) }

    factory { GetTasksUseCase(get()) }
    factory { CreateTaskUseCase(get()) }
    factory { UpdateTaskUseCase(get()) }
    factory { DeleteTaskUseCase(get()) }
    factory { ToggleTaskUseCase(get()) }
    factory { SearchTasksUseCase(get()) }

    factory { GetHabitsUseCase(get()) }
    factory { CreateHabitUseCase(get()) }
    factory { UpdateHabitUseCase(get()) }
    factory { DeleteHabitUseCase(get()) }
    factory { CalculateStreakUseCase() }
    factory { CheckInHabitUseCase(get(), get(), get()) }

    factory { PomodoroSessionUseCase(get()) }

    factory { UnlockAchievementUseCase(get()) }
}
