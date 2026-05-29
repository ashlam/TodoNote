package com.todonote.di

import androidx.room.Room
import com.todonote.data.local.TodoNoteDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            TodoNoteDatabase::class.java,
            "todonote.db"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<TodoNoteDatabase>().listDao() }
    single { get<TodoNoteDatabase>().taskDao() }
    single { get<TodoNoteDatabase>().tagDao() }
    single { get<TodoNoteDatabase>().habitDao() }
    single { get<TodoNoteDatabase>().habitCheckInDao() }
    single { get<TodoNoteDatabase>().pomodoroSessionDao() }
    single { get<TodoNoteDatabase>().achievementDao() }
    single { get<TodoNoteDatabase>().taskTagCrossRefDao() }
}
