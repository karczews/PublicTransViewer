package com.github.karczews.publictarnsvisualizer.di

import dev.zacsweers.metrox.viewmodel.MetroViewModelFactory

/**
 * Implemented by the host [android.app.Application] so UI code can obtain the Metro
 * [MetroViewModelFactory] without depending on a concrete graph type. Both the production app
 * and the instrumented-test app implement this, which is what lets the same `MainActivity` run
 * against either the real graph or the test graph (the role Hilt's component swapping played).
 */
interface AppGraphOwner {
    val viewModelFactory: MetroViewModelFactory
}
