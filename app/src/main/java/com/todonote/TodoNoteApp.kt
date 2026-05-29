package com.todonote

import android.app.Application
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.work.Configuration
import androidx.work.WorkManager
import com.todonote.data.local.backup.BackupScheduler
import com.todonote.di.appModule
import com.todonote.di.databaseModule
import com.todonote.di.repositoryModule
import com.todonote.di.useCaseModule
import com.todonote.di.viewModelModule
import com.todonote.presentation.settings.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class TodoNoteApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@TodoNoteApp)
            modules(
                appModule,
                databaseModule,
                repositoryModule,
                useCaseModule,
                viewModelModule
            )
        }

        // Initialize auto backup if enabled
        runBlocking {
            val prefs = dataStore.data.first()
            val autoBackup = prefs[booleanPreferencesKey("auto_backup")] ?: false
            if (autoBackup) {
                BackupScheduler.schedule(this@TodoNoteApp)
            }
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
}
