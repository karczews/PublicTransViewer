package com.github.karczews.publictarnsvisualizer.di

import com.github.karczews.publictarnsvisualizer.FixedAlertRepository
import com.github.karczews.publictarnsvisualizer.FixedRouteDisplayRepository
import com.github.karczews.publictarnsvisualizer.FixedStopRepository
import com.github.karczews.publictarnsvisualizer.FixedVehicleRepository
import android.content.Context
import androidx.room.Room
import com.github.karczews.publictarnsvisualizer.data.db.GtfsDatabase
import com.github.karczews.publictarnsvisualizer.data.repository.AlertRepository
import com.github.karczews.publictarnsvisualizer.data.repository.RouteDisplayRepository
import com.github.karczews.publictarnsvisualizer.data.repository.StopRepository
import com.github.karczews.publictarnsvisualizer.data.repository.VehicleRepository
import com.github.karczews.publictarnsvisualizer.data.source.GtfsStaticDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class, InfrastructureModule::class],
)
object FakeAppModule {

    @Provides
    @Singleton
    fun provideVehicleRepository(): VehicleRepository = FixedVehicleRepository()

    @Provides
    @Singleton
    fun provideRouteDisplayRepository(): RouteDisplayRepository = FixedRouteDisplayRepository()

    @Provides
    @Singleton
    fun provideAlertRepository(): AlertRepository = FixedAlertRepository()

    @Provides
    @Singleton
    fun provideStopRepository(): StopRepository = FixedStopRepository()

    @Provides
    @Singleton
    fun provideGtfsStaticDataSource(
        @ApplicationContext context: Context,
    ): GtfsStaticDataSource {
        val db = Room.inMemoryDatabaseBuilder(context, GtfsDatabase::class.java).build()
        return GtfsStaticDataSource(OkHttpClient(), db)
    }
}
