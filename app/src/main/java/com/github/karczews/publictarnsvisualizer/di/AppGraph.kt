package com.github.karczews.publictarnsvisualizer.di

import android.app.Application
import androidx.work.ListenableWorker
import com.github.karczews.publictarnsvisualizer.data.source.GtfsStaticDataSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Multibinds
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelGraph
import kotlin.reflect.KClass

/**
 * The application-wide Metro dependency graph. Replaces Hilt's `SingletonComponent`.
 *
 * Bindings are aggregated from every [dev.zacsweers.metro.ContributesTo],
 * [dev.zacsweers.metro.ContributesBinding] and [dev.zacsweers.metro.ContributesIntoMap]
 * contribution targeting [AppScope] — namely [InfrastructureBindings], [RepositoryBindings],
 * the `@ViewModelKey` ViewModels and the `@WorkerKey` worker factory.
 *
 * Extending [ViewModelGraph] exposes the ViewModel multibinding maps consumed by
 * [InjectedViewModelFactory].
 */
@DependencyGraph(AppScope::class)
interface AppGraph : ViewModelGraph {

    /** Factory backing `metroViewModel()` in Compose (the replacement for `hiltViewModel()`). */
    val viewModelFactory: MetroViewModelFactory

    /** WorkManager factory that constructs Metro-injected workers (replaces `HiltWorkerFactory`). */
    val workerFactory: MetroWorkerFactory

    /** Used by the Application to trigger the initial GTFS download. */
    val gtfsStaticDataSource: GtfsStaticDataSource

    @Multibinds
    val workerProviders:
        Map<KClass<out ListenableWorker>, () -> MetroWorkerFactory.WorkerInstanceFactory<*>>

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides application: Application): AppGraph
    }
}
