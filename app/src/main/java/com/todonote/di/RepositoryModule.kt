package com.todonote.di

import com.todonote.data.repository.*
import com.todonote.domain.repository.*
import org.koin.dsl.module

val repositoryModule = module {
    single<IListRepository> { ListRepository(get()) }
    single<ITaskRepository> { TaskRepository(get()) }
    single<ITagRepository> { TagRepository(get()) }
    single<IHabitRepository> { HabitRepository(get()) }
    single<IPomodoroRepository> { PomodoroRepository(get()) }
    single<IAchievementRepository> { AchievementRepository(get()) }
}
