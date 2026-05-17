package com.github.karczews.publictarnsvisualizer

import android.app.Application
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.github.karczews.publictarnsvisualizer.data.db.GtfsDatabase
import com.github.karczews.publictarnsvisualizer.data.network.GtfsRtApi
import com.github.karczews.publictarnsvisualizer.data.repository.AlertRepository
import com.github.karczews.publictarnsvisualizer.data.repository.DefaultAlertRepository
import com.github.karczews.publictarnsvisualizer.data.repository.DefaultStopRepository
import com.github.karczews.publictarnsvisualizer.data.repository.DefaultVehicleRepository
import com.github.karczews.publictarnsvisualizer.data.repository.RouteDisplayRepository
import com.github.karczews.publictarnsvisualizer.data.repository.StopRepository
import com.github.karczews.publictarnsvisualizer.data.repository.VehicleRepository
import com.github.karczews.publictarnsvisualizer.data.source.GtfsRtAlertDataSource
import com.github.karczews.publictarnsvisualizer.data.source.GtfsRtTripUpdateDataSource
import com.github.karczews.publictarnsvisualizer.data.source.GtfsRtVehicleDataSource
import com.github.karczews.publictarnsvisualizer.data.source.GtfsStaticDataSource
import com.github.karczews.publictarnsvisualizer.data.worker.GtfsRefreshWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class PublicTransApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val database: GtfsDatabase by lazy {
        Room.databaseBuilder(this, GtfsDatabase::class.java, "gtfs.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()
    }

    private val gtfsRtApi: GtfsRtApi by lazy { GtfsRtApi(httpClient) }

    private val vehicleDataSource: GtfsRtVehicleDataSource by lazy {
        GtfsRtVehicleDataSource(gtfsRtApi)
    }

    private val tripUpdateDataSource: GtfsRtTripUpdateDataSource by lazy {
        GtfsRtTripUpdateDataSource(gtfsRtApi)
    }

    private val alertDataSource: GtfsRtAlertDataSource by lazy {
        GtfsRtAlertDataSource(gtfsRtApi)
    }

    val gtfsStaticDataSource: GtfsStaticDataSource by lazy {
        GtfsStaticDataSource(httpClient, database)
    }

    val vehicleRepository: VehicleRepository by lazy {
        DefaultVehicleRepository(vehicleDataSource, database.routeDao(), database.tripDao())
    }

    val routeDisplayRepository: RouteDisplayRepository by lazy {
        RouteDisplayRepository(
            tripDao = database.tripDao(),
            routeDao = database.routeDao(),
            shapePointDao = database.shapePointDao(),
            stopTimeDao = database.stopTimeDao(),
            stopDao = database.stopDao(),
        )
    }

    val alertRepository: AlertRepository by lazy {
        DefaultAlertRepository(alertDataSource, database.routeDao())
    }

    val stopRepository: StopRepository by lazy {
        DefaultStopRepository(
            stopDao = database.stopDao(),
            stopTimeDao = database.stopTimeDao(),
            tripDao = database.tripDao(),
            routeDao = database.routeDao(),
            tripUpdateDataSource = tripUpdateDataSource,
        )
    }

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
                android.util.Log.d("PublicTransApp", "Starting initial GTFS download")
                val result = gtfsStaticDataSource.downloadAndImport()
                android.util.Log.d("PublicTransApp", "GTFS download result: $result")
                result.exceptionOrNull()?.let {
                    android.util.Log.e("PublicTransApp", "GTFS download failed", it)
                }
            } else {
                android.util.Log.d("PublicTransApp", "GTFS data already available")
            }
        }
    }
}
