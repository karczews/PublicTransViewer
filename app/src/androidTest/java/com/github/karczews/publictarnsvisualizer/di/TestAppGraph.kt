package com.github.karczews.publictarnsvisualizer.di

import android.app.Application
import com.github.karczews.publictarnsvisualizer.FixedAlertRepository
import com.github.karczews.publictarnsvisualizer.FixedRouteDisplayRepository
import com.github.karczews.publictarnsvisualizer.FixedStopRepository
import com.github.karczews.publictarnsvisualizer.FixedVehicleRepository
import com.github.karczews.publictarnsvisualizer.data.repository.AlertRepository
import com.github.karczews.publictarnsvisualizer.data.repository.RouteDisplayRepository
import com.github.karczews.publictarnsvisualizer.data.repository.StopRepository
import com.github.karczews.publictarnsvisualizer.data.repository.VehicleRepository
import com.github.karczews.publictarnsvisualizer.data.worker.GtfsRefreshWorker
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

/**
 * Test variant of [AppGraph] used by instrumented UI tests. Replaces Hilt's
 * `@TestInstallIn(replaces = [RepositoryModule, InfrastructureModule])` + `HiltTestApplication`.
 *
 * Instead of replacing whole modules, this graph `excludes` the production [RepositoryBindings]
 * (and the GTFS worker contribution) and provides in-memory fakes for the four repositories. The
 * ViewModels are still aggregated from `@ContributesIntoMap`, so the same `metroViewModel()` calls
 * in `MainActivity` resolve here — but against the fakes.
 */
@DependencyGraph(
    AppScope::class,
    excludes = [RepositoryBindings::class, GtfsRefreshWorker.Factory::class],
)
interface TestAppGraph : ViewModelGraph {

    val viewModelFactory: MetroViewModelFactory

    @Provides
    @SingleIn(AppScope::class)
    fun provideVehicleRepository(): VehicleRepository = FixedVehicleRepository()

    @Provides
    @SingleIn(AppScope::class)
    fun provideRouteDisplayRepository(): RouteDisplayRepository = FixedRouteDisplayRepository()

    @Provides
    @SingleIn(AppScope::class)
    fun provideAlertRepository(): AlertRepository = FixedAlertRepository()

    @Provides
    @SingleIn(AppScope::class)
    fun provideStopRepository(): StopRepository = FixedStopRepository()

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): TestAppGraph
    }
}
