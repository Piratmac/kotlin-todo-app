package com.piratmac.todo.views.helpers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class BackupWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    private val backupManager = BackupManager(appContext)

    companion object {
        const val WORK_NAME = "com.piratmac.todo.views.helpers.BackupScheduler"
    }

    override suspend fun doWork(): Result {
        backupManager.setBackupFile("tasks.db")
        val result = backupManager.backupDatabaseToFile("tasks")
        return if (result) Result.success()
        else Result.failure()
    }


}
