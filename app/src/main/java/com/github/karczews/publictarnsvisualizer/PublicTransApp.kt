package com.github.karczews.publictarnsvisualizer

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.karczews.publictarnsvisualizer.data.source.GtfsStaticDataSource
import com.github.karczews.publictarnsvisualizer.data.worker.GtfsRefreshWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class PublicTransApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var gtfsStaticDataSource: GtfsStaticDataSource

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
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
