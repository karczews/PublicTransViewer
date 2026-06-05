package com.github.karczews.publictarnsvisualizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.karczews.publictarnsvisualizer.di.AppGraphOwner
import com.github.karczews.publictarnsvisualizer.ui.alerts.AlertsScreen
import com.github.karczews.publictarnsvisualizer.ui.alerts.AlertsViewModel
import com.github.karczews.publictarnsvisualizer.ui.home.HomeScreen
import com.github.karczews.publictarnsvisualizer.ui.home.HomeViewModel
import com.github.karczews.publictarnsvisualizer.ui.stops.StopsScreen
import com.github.karczews.publictarnsvisualizer.ui.stops.StopsViewModel
import com.github.karczews.publictarnsvisualizer.ui.theme.PublicTarnsVisualizerTheme
import com.tomtom.sdk.common.configuration.buildSdkConfiguration
import com.tomtom.sdk.init.TomTomSdk
import com.tomtom.sdk.telemetry.UserConsent
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory
import dev.zacsweers.metrox.viewmodel.metroViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val sdkInitialized = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initTomTomSdk()
        val viewModelFactory = (application as AppGraphOwner).viewModelFactory
        setContent {
            PublicTarnsVisualizerTheme {
                val isReady by sdkInitialized.collectAsStateWithLifecycle()
                if (isReady) {
                    CompositionLocalProvider(LocalMetroViewModelFactory provides viewModelFactory) {
                        PublicTarnsVisualizerApp()
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    private fun initTomTomSdk() {
        lifecycleScope.launch {
            if (!TomTomSdk.isInitialized) {
                TomTomSdk.initialize(
                    context = application,
                    sdkConfiguration = buildSdkConfiguration(
                        context = application,
                        apiKey = BuildConfig.TOMTOM_API_KEY,
                        telemetryUserConsent = { UserConsent.TelemetryOn },
                    ),
                )
            }
            sdkInitialized.value = true
        }
    }
}

@Composable
fun PublicTarnsVisualizerApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.HOME) }

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            AppDestinations.entries.forEach {
                item(
                    icon = {
                        Icon(
                            painterResource(it.icon),
                            contentDescription = it.label
                        )
                    },
                    label = { Text(it.label) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
                )
            }
        }
    ) {
        when (currentDestination) {
            AppDestinations.HOME -> {
                val homeViewModel: HomeViewModel = metroViewModel()
                HomeScreen(viewModel = homeViewModel)
            }
            AppDestinations.STOPS -> {
                val stopsViewModel: StopsViewModel = metroViewModel()
                StopsScreen(viewModel = stopsViewModel)
            }
            AppDestinations.ALERTS -> {
                val alertsViewModel: AlertsViewModel = metroViewModel()
                AlertsScreen(viewModel = alertsViewModel)
            }
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    HOME("Home", R.drawable.ic_home),
    STOPS("Stops", R.drawable.ic_bus),
    ALERTS("Alerts", R.drawable.ic_alert),
}
