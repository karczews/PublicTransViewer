package com.github.karczews.publictarnsvisualizer.di

import android.app.Application
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
import com.github.karczews.publictarnsvisualizer.data.source.AlertDataSource
import com.github.karczews.publictarnsvisualizer.data.source.GtfsRtAlertDataSource
import com.github.karczews.publictarnsvisualizer.data.source.GtfsRtTripUpdateDataSource
import com.github.karczews.publictarnsvisualizer.data.source.GtfsRtVehicleDataSource
import com.github.karczews.publictarnsvisualizer.data.source.GtfsStaticDataSource
import com.github.karczews.publictarnsvisualizer.data.source.TripUpdateDataSource
import com.github.karczews.publictarnsvisualizer.data.source.VehicleDataSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Networking, database and data-source bindings.
 *
 * Direct translation of Hilt's `InfrastructureModule` (`@Module @InstallIn(SingletonComponent)`):
 * a `@ContributesTo(AppScope)` interface whose `@Provides` functions are merged into every
 * graph scoped to [AppScope]. `@SingleIn(AppScope::class)` replaces `@Singleton`.
 */
@ContributesTo(AppScope::class)
interface InfrastructureBindings {

    // Replaces Hilt's `@ApplicationContext Context`: the Application is bound by the graph factory.
    @Provides
    fun provideApplicationContext(application: Application): Context = application

    @Provides
    @SingleIn(AppScope::class)
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    @Provides
    @SingleIn(AppScope::class)
    fun provideGtfsDatabase(context: Context): GtfsDatabase =
        Room.databaseBuilder(context, GtfsDatabase::class.java, "gtfs.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun provideRouteDao(db: GtfsDatabase): RouteDao = db.routeDao()
    @Provides fun provideStopDao(db: GtfsDatabase): StopDao = db.stopDao()
    @Provides fun provideTripDao(db: GtfsDatabase): TripDao = db.tripDao()
    @Provides fun provideStopTimeDao(db: GtfsDatabase): StopTimeDao = db.stopTimeDao()
    @Provides fun provideShapePointDao(db: GtfsDatabase): ShapePointDao = db.shapePointDao()

    @Provides
    @SingleIn(AppScope::class)
    fun provideGtfsRtApi(client: OkHttpClient): GtfsRtApi = GtfsRtApiImpl(client)

    @Provides
    @SingleIn(AppScope::class)
    fun provideVehicleDataSource(api: GtfsRtApi): VehicleDataSource = GtfsRtVehicleDataSource(api)

    @Provides
    @SingleIn(AppScope::class)
    fun provideTripUpdateDataSource(api: GtfsRtApi): TripUpdateDataSource = GtfsRtTripUpdateDataSource(api)

    @Provides
    @SingleIn(AppScope::class)
    fun provideAlertDataSource(api: GtfsRtApi): AlertDataSource = GtfsRtAlertDataSource(api)

    @Provides
    @SingleIn(AppScope::class)
    fun provideGtfsStaticDataSource(client: OkHttpClient, db: GtfsDatabase): GtfsStaticDataSource =
        GtfsStaticDataSource(client, db)
}
