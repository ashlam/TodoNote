package com.todonote.data.local.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DailyBackupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val backupManager: BackupManager by inject()

    override suspend fun doWork(): Result {
        return backupManager.exportToJson().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.failure() }
        )
    }
}
