package com.github.karczews.publictarnsvisualizer.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import dev.zacsweers.metro.Inject
import kotlin.reflect.KClass

/**
 * A WorkManager [WorkerFactory] that constructs workers from a Metro multibinding map keyed by
 * worker class. Replaces Hilt's `HiltWorkerFactory` (`@HiltWorker` + `hilt-work`).
 *
 * Each worker contributes an assisted-injection factory into [workerProviders] via
 * `@WorkerKey(...) @ContributesIntoMap(AppScope::class)` on its `@AssistedFactory`.
 */
@Inject
class MetroWorkerFactory(
    private val workerProviders: Map<KClass<out ListenableWorker>, WorkerInstanceFactory<*>>,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? =
        workerProviders[Class.forName(workerClassName).kotlin]?.create(workerParameters)

    /** Assisted factory contract implemented by each worker's `@AssistedFactory`. */
    interface WorkerInstanceFactory<T : ListenableWorker> {
        fun create(params: WorkerParameters): T
    }
}
