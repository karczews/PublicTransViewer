package com.github.karczews.publictarnsvisualizer.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.karczews.publictarnsvisualizer.data.source.GtfsStaticDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class GtfsRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val gtfsStaticDataSource: GtfsStaticDataSource,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return gtfsStaticDataSource.downloadAndImport().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    companion object {
        const val WORK_NAME = "gtfs_refresh"
    }
}
