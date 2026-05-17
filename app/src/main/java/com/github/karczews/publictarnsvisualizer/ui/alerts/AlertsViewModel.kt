package com.github.karczews.publictarnsvisualizer.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.karczews.publictarnsvisualizer.data.model.ServiceAlert
import com.github.karczews.publictarnsvisualizer.data.repository.AlertRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AlertsViewModel(
    private val repository: AlertRepository,
) : ViewModel() {

    private val _alerts = MutableStateFlow<List<ServiceAlert>>(emptyList())
    val alerts: StateFlow<List<ServiceAlert>> = _alerts.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAlerts()
                .catch { /* keep last known */ }
                .collect { alerts ->
                    _alerts.value = alerts
                    _isLoading.value = false
                }
        }
    }
}

class AlertsViewModelFactory(
    private val repository: AlertRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AlertsViewModel(repository) as T
    }
}
