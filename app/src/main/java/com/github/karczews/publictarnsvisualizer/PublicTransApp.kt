package com.github.karczews.publictarnsvisualizer

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.karczews.publictarnsvisualizer.data.worker.GtfsRefreshWorker
import com.github.karczews.publictarnsvisualizer.di.AppGraph
import com.github.karczews.publictarnsvisualizer.di.AppGraphOwner
import dev.zacsweers.metro.createGraphFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PublicTransApp : Application(), Configuration.Provider, AppGraphOwner {

    // Replaces Hilt's @HiltAndroidApp-generated component. The graph is created eagerly-on-first-use
    // with the Application bound as a graph input via AppGraph.Factory.
    val appGraph: AppGraph by lazy { createGraphFactory<AppGraph.Factory>().create(this) }

    override val viewModelFactory: MetroViewModelFactory
        get() = appGraph.viewModelFactory

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Replaces the injected HiltWorkerFactory.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(appGraph.workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        scheduleGtfsRefresh()
        triggerInitialGtfsDownloadIfNeeded()
    }

    private fun scheduleGtfsRefresh() {
        val periodicRequest = PeriodicWorkRequestBuilder<GtfsRefreshWorker>(7, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            GtfsRefreshWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest,
        )
    }

    private fun triggerInitialGtfsDownloadIfNeeded() {
        val gtfsStaticDataSource = appGraph.gtfsStaticDataSource
        appScope.launch {
            if (!gtfsStaticDataSource.isDataAvailable()) {
                Log.d("PublicTransApp", "Starting initial GTFS download")
                val result = gtfsStaticDataSource.downloadAndImport()
                Log.d("PublicTransApp", "GTFS download result: $result")
                result.exceptionOrNull()?.let {
                    Log.e("PublicTransApp", "GTFS download failed", it)
                }
            } else {
                Log.d("PublicTransApp", "GTFS data already available")
            }
        }
    }
}
