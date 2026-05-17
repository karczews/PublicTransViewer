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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InfrastructureModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideGtfsDatabase(@ApplicationContext context: Context): GtfsDatabase =
        Room.databaseBuilder(context, GtfsDatabase::class.java, "gtfs.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideRouteDao(db: GtfsDatabase): RouteDao = db.routeDao()
    @Provides fun provideStopDao(db: GtfsDatabase): StopDao = db.stopDao()
    @Provides fun provideTripDao(db: GtfsDatabase): TripDao = db.tripDao()
    @Provides fun provideStopTimeDao(db: GtfsDatabase): StopTimeDao = db.stopTimeDao()
    @Provides fun provideShapePointDao(db: GtfsDatabase): ShapePointDao = db.shapePointDao()

    @Provides
    @Singleton
    fun provideGtfsRtApi(client: OkHttpClient): GtfsRtApi = GtfsRtApiImpl(client)

    @Provides
    @Singleton
    fun provideVehicleDataSource(api: GtfsRtApi): VehicleDataSource = GtfsRtVehicleDataSource(api)

    @Provides
    @Singleton
    fun provideTripUpdateDataSource(api: GtfsRtApi): TripUpdateDataSource = GtfsRtTripUpdateDataSource(api)

    @Provides
    @Singleton
    fun provideAlertDataSource(api: GtfsRtApi): AlertDataSource = GtfsRtAlertDataSource(api)

    @Provides
    @Singleton
    fun provideGtfsStaticDataSource(client: OkHttpClient, db: GtfsDatabase): GtfsStaticDataSource =
        GtfsStaticDataSource(client, db)
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideVehicleRepository(
        dataSource: VehicleDataSource,
        routeDao: RouteDao,
        tripDao: TripDao,
    ): VehicleRepository = DefaultVehicleRepository(dataSource, routeDao, tripDao)

    @Provides
    @Singleton
    fun provideRouteDisplayRepository(
        tripDao: TripDao,
        routeDao: RouteDao,
        shapePointDao: ShapePointDao,
        stopTimeDao: StopTimeDao,
        stopDao: StopDao,
    ): RouteDisplayRepository = DefaultRouteDisplayRepository(tripDao, routeDao, shapePointDao, stopTimeDao, stopDao)

    @Provides
    @Singleton
    fun provideAlertRepository(
        alertDataSource: AlertDataSource,
        routeDao: RouteDao,
    ): AlertRepository = DefaultAlertRepository(alertDataSource, routeDao)

    @Provides
    @Singleton
    fun provideStopRepository(
        stopDao: StopDao,
        stopTimeDao: StopTimeDao,
        tripDao: TripDao,
        routeDao: RouteDao,
        tripUpdateDataSource: TripUpdateDataSource,
    ): StopRepository = DefaultStopRepository(stopDao, stopTimeDao, tripDao, routeDao, tripUpdateDataSource)
}
