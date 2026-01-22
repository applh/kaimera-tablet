package com.kaimera.tablet.features.maps

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MapsViewModel @Inject constructor() : ViewModel() {

    private val _centerLat = MutableStateFlow(0.0)
    val centerLat: StateFlow<Double> = _centerLat.asStateFlow()

    private val _centerLon = MutableStateFlow(0.0)
    val centerLon: StateFlow<Double> = _centerLon.asStateFlow()

    private val _zoomLevel = MutableStateFlow(2.0)
    val zoomLevel: StateFlow<Double> = _zoomLevel.asStateFlow()

    fun updateMapState(lat: Double, lon: Double, zoom: Double) {
        _centerLat.value = lat
        _centerLon.value = lon
        _zoomLevel.value = zoom
    }
}
