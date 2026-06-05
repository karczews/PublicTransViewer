package com.github.karczews.publictarnsvisualizer.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import dev.zacsweers.metrox.viewmodel.ManualViewModelAssistedFactory
import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.ViewModelAssistedFactory
import kotlin.reflect.KClass

/**
 * A [ViewModelProvider.Factory] backed by the Metro-injected multibinding maps of ViewModel
 * providers. This is the Metro equivalent of the factory that Hilt generated behind
 * `@HiltViewModel` + `hiltViewModel()`.
 *
 * The maps are supplied by [dev.zacsweers.metrox.viewmodel.ViewModelGraph] and populated by
 * every ViewModel annotated with `@ContributesIntoMap(AppScope::class) @ViewModelKey @Inject`.
 */
@ContributesBinding(AppScope::class)
@ContributesBinding(AppScope::class, binding<ViewModelProvider.Factory>())
@Inject
class InjectedViewModelFactory(
    override val viewModelProviders: Map<KClass<out ViewModel>, () -> ViewModel>,
    override val assistedFactoryProviders: Map<KClass<out ViewModel>, () -> ViewModelAssistedFactory>,
    override val manualAssistedFactoryProviders:
        Map<KClass<out ManualViewModelAssistedFactory>, () -> ManualViewModelAssistedFactory>,
) : MetroViewModelFactory()
