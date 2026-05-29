package com.todonote.di

import com.todonote.data.local.backup.BackupManager
import org.koin.dsl.module

val appModule = module {
    single { BackupManager(get(), get()) }
}
