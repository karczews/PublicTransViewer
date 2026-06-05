package com.github.karczews.publictarnsvisualizer.di

import com.github.karczews.publictarnsvisualizer.data.db.dao.RouteDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.ShapePointDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.StopDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.StopTimeDao
import com.github.karczews.publictarnsvisualizer.data.db.dao.TripDao
import com.github.karczews.publictarnsvisualizer.data.repository.AlertRepository
import com.github.karczews.publictarnsvisualizer.data.repository.DefaultAlertRepository
import com.github.karczews.publictarnsvisualizer.data.repository.DefaultRouteDisplayRepository
import com.github.karczews.publictarnsvisualizer.data.repository.DefaultStopRepository
import com.github.karczews.publictarnsvisualizer.data.repository.DefaultVehicleRepository
import com.github.karczews.publictarnsvisualizer.data.repository.RouteDisplayRepository
import com.github.karczews.publictarnsvisualizer.data.repository.StopRepository
import com.github.karczews.publictarnsvisualizer.data.repository.VehicleRepository
import com.github.karczews.publictarnsvisualizer.data.source.AlertDataSource
import com.github.karczews.publictarnsvisualizer.data.source.TripUpdateDataSource
import com.github.karczews.publictarnsvisualizer.data.source.VehicleDataSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

/**
 * Repository bindings — direct translation of Hilt's `RepositoryModule`.
 *
 * Kept as explicit `@Provides` functions (rather than `@ContributesBinding` on the `Default*`
 * classes) so the repository implementations stay free of DI annotations, and because
 * [DefaultStopRepository] has a defaulted `clock` constructor parameter that is not a graph
 * binding. Instrumented tests swap the whole container out via `excludes = [RepositoryBindings::class]`.
 */
@ContributesTo(AppScope::class)
interface RepositoryBindings {

    @Provides
    @SingleIn(AppScope::class)
    fun provideVehicleRepository(
        dataSource: VehicleDataSource,
        routeDao: RouteDao,
        tripDao: TripDao,
    ): VehicleRepository = DefaultVehicleRepository(dataSource, routeDao, tripDao)

    @Provides
    @SingleIn(AppScope::class)
    fun provideRouteDisplayRepository(
        tripDao: TripDao,
        routeDao: RouteDao,
        shapePointDao: ShapePointDao,
        stopTimeDao: StopTimeDao,
        stopDao: StopDao,
    ): RouteDisplayRepository =
        DefaultRouteDisplayRepository(tripDao, routeDao, shapePointDao, stopTimeDao, stopDao)

    @Provides
    @SingleIn(AppScope::class)
    fun provideAlertRepository(
        alertDataSource: AlertDataSource,
        routeDao: RouteDao,
    ): AlertRepository = DefaultAlertRepository(alertDataSource, routeDao)

    @Provides
    @SingleIn(AppScope::class)
    fun provideStopRepository(
        stopDao: StopDao,
        stopTimeDao: StopTimeDao,
        tripDao: TripDao,
        routeDao: RouteDao,
        tripUpdateDataSource: TripUpdateDataSource,
    ): StopRepository =
        DefaultStopRepository(stopDao, stopTimeDao, tripDao, routeDao, tripUpdateDataSource)
}
