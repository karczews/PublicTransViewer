package com.github.karczews.publictarnsvisualizer

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Swaps in [TestPublicTransApp] for instrumented tests so `MainActivity` resolves ViewModels
 * from the [com.github.karczews.publictarnsvisualizer.di.TestAppGraph] (fakes) rather than the
 * production graph. Replaces the Hilt `HiltTestApplication`-based runner.
 */
class MetroTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, TestPublicTransApp::class.java.name, context)
    }
}
