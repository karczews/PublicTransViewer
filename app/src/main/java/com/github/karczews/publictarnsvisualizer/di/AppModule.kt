package com.github.karczews.publictarnsvisualizer.di

import android.content.Context
import androidx.room.Room
import com.github.karczews.publictarnsvisualizer.data.db.GtfsDatabase
import com.github.karczews.publictarnsvisualizer.data.db.dao.RouteDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.ShapePointDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.StopDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.StopTimeDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.TripDao
import com.github.karczews.publictarnsvisualizer.data.network.GtfsRtApi
import com.github.karczews.publictarnsvisualizer.data.network.GtfsRtApiImpl
import com.github.karczews.publictarnsvisualizer.data.repository.AlertRepository
import com.github.karczews.publictarnsvisualizer.data.repository.DefaultAlertRepository
import com.github.karczews.publictarnsvisualizer.data.repository.DefaultRouteDisplayRepository
import com.github.karczews.publictarnsvisualizer.data.repository.DefaultStopRepository
import com.github.karczews.publictarnsvisualizer.data.repository.DefaultVehicleRepository
import com.github.karczews.publictarnsvisualizer.data.repository.RouteDisplayRepository
import com.github.karczews.publictarnsvisualizer.data.repository.StopRepository
import com.github.karczews.publictarnsvisualizer.data.repository.VehicleRepository
import com.github.karczews.publictarnsvisualizer.data.source.AlertDataSource
import com.github.karczews.publictarnsvisualizer.data.source.GtfsRtAlertDataSource
import com.github.karczews.publictarnsvisualizer.data.source.GtfsRtTripUpdateDataSource
import com.github.karczews.publictarnsvisualizer.data.source.GtfsRtVehicleDataSource
import com.github.karczews.publictarnsvisualizer.data.source.GtfsStaticDataSource
import com.github.karczews.publictarnsvisualizer.data.source.TripUpdateDataSource
import com.github.karczews.publictarnsvisualizer.data.source.VehicleDataSource
import okhttp3.OkHttpClient
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import java.util.concurrent.TimeUnit

/**
 * Koin module wiring the whole graph.
 *
 * Infrastructure, data sources and repositories are declared as [Single] provider functions
 * (the equivalent of Dagger `@Provides`) because they need custom construction or interface
 * binding. The [ComponentScan] picks up class-level definitions in the base package — the
 * `@KoinViewModel` view models and the `@KoinWorker` worker.
 *
 * The Koin compiler plugin validates this graph at compile time (`compileSafety`); `android.content.Context`
 * is part of the framework whitelist and is supplied at runtime by `androidContext()`.
 */
@Module
@ComponentScan("com.github.karczews.publictarnsvisualizer")
class AppModule {

    @Single
    fun okHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Single
    fun gtfsDatabase(context: Context): GtfsDatabase =
        Room.databaseBuilder(context, GtfsDatabase::class.java, "gtfs.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Single fun routeDao(db: GtfsDatabase): RouteDao = db.routeDao()
    @Single fun stopDao(db: GtfsDatabase): StopDao = db.stopDao()
    @Single fun tripDao(db: GtfsDatabase): TripDao = db.tripDao()
    @Single fun stopTimeDao(db: GtfsDatabase): StopTimeDao = db.stopTimeDao()
    @Single fun shapePointDao(db: GtfsDatabase): ShapePointDao = db.shapePointDao()

    @Single
    fun gtfsRtApi(client: OkHttpClient): GtfsRtApi = GtfsRtApiImpl(client)

    @Single
    fun vehicleDataSource(api: GtfsRtApi): VehicleDataSource = GtfsRtVehicleDataSource(api)

    @Single
    fun tripUpdateDataSource(api: GtfsRtApi): TripUpdateDataSource = GtfsRtTripUpdateDataSource(api)

    @Single
    fun alertDataSource(api: GtfsRtApi): AlertDataSource = GtfsRtAlertDataSource(api)

    @Single
    fun gtfsStaticDataSource(client: OkHttpClient, db: GtfsDatabase): GtfsStaticDataSource =
        GtfsStaticDataSource(client, db)

    @Single
    fun vehicleRepository(
        dataSource: VehicleDataSource,
        routeDao: RouteDao,
        tripDao: TripDao,
    ): VehicleRepository = DefaultVehicleRepository(dataSource, routeDao, tripDao)

    @Single
    fun routeDisplayRepository(
        tripDao: TripDao,
        routeDao: RouteDao,
        shapePointDao: ShapePointDao,
        stopTimeDao: StopTimeDao,
        stopDao: StopDao,
    ): RouteDisplayRepository = DefaultRouteDisplayRepository(tripDao, routeDao, shapePointDao, stopTimeDao, stopDao)

    @Single
    fun alertRepository(
        alertDataSource: AlertDataSource,
        routeDao: RouteDao,
    ): AlertRepository = DefaultAlertRepository(alertDataSource, routeDao)

    @Single
    fun stopRepository(
        stopDao: StopDao,
        stopTimeDao: StopTimeDao,
        tripDao: TripDao,
        routeDao: RouteDao,
        tripUpdateDataSource: TripUpdateDataSource,
    ): StopRepository = DefaultStopRepository(stopDao, stopTimeDao, tripDao, routeDao, tripUpdateDataSource)
}
