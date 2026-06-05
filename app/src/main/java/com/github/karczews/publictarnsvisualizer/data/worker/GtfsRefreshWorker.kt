package com.github.karczews.publictarnsvisualizer.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.github.karczews.publictarnsvisualizer.data.source.GtfsStaticDataSource
import com.github.karczews.publictarnsvisualizer.di.MetroWorkerFactory
import com.github.karczews.publictarnsvisualizer.di.WorkerKey
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.binding

@AssistedInject
class GtfsRefreshWorker(
    appContext: Context,
    @Assisted params: WorkerParameters,
    private val gtfsStaticDataSource: GtfsStaticDataSource,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return gtfsStaticDataSource.downloadAndImport().fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    /**
     * Assisted factory contributed into the worker multibinding map consumed by
     * [MetroWorkerFactory]. Replaces Hilt's `@HiltWorker` code generation.
     */
    @WorkerKey(GtfsRefreshWorker::class)
    @ContributesIntoMap(
        AppScope::class,
        binding = binding<MetroWorkerFactory.WorkerInstanceFactory<*>>(),
    )
    @AssistedFactory
    abstract class Factory : MetroWorkerFactory.WorkerInstanceFactory<GtfsRefreshWorker>

    companion object {
        const val WORK_NAME = "gtfs_refresh"
    }
}
