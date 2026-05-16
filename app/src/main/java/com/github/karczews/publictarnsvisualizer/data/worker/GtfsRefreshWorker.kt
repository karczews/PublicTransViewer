package com.github.karczews.publictarnsvisualizer.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.karczews.publictarnsvisualizer.PublicTransApp

class GtfsRefreshWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as PublicTransApp
        return app.gtfsStaticDataSource.downloadAndImport().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    companion object {
        const val WORK_NAME = "gtfs_refresh"
    }
}
