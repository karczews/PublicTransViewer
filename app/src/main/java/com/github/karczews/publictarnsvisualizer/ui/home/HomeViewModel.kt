package com.github.karczews.publictarnsvisualizer.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.github.karczews.publictarnsvisualizer.data.model.VehiclePosition
import com.github.karczews.publictarnsvisualizer.data.repository.VehicleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: VehicleRepository,
) : ViewModel() {

    private val _vehiclePositions = MutableStateFlow<List<VehiclePosition>>(emptyList())
    val vehiclePositions: StateFlow<List<VehiclePosition>> = _vehiclePositions.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeVehiclePositions()
                .catch { e ->
                    Log.e("HomeViewModel", "Flow error", e)
                    _error.value = e.message
                }
                .collect { positions ->
                    Log.d("HomeViewModel", "Received ${positions.size} vehicles")
                    _vehiclePositions.value = positions
                    if (positions.isNotEmpty()) _error.value = null
                }
        }
    }
}

class HomeViewModelFactory(
    private val repository: VehicleRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return HomeViewModel(repository) as T
    }
}
