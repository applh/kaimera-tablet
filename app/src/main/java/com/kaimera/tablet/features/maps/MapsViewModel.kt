package com.kaimera.tablet.features.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

@HiltViewModel
class MapsViewModel @Inject constructor(
    private val repository: com.kaimera.tablet.core.data.UserPreferencesRepository,
    private val mapsRepository: com.kaimera.tablet.features.maps.data.MapsRepository
) : ViewModel() {

    private val _centerLat = MutableStateFlow(0.0)
    val centerLat: StateFlow<Double> = _centerLat.asStateFlow()

    private val _centerLon = MutableStateFlow(0.0)
    val centerLon: StateFlow<Double> = _centerLon.asStateFlow()

    private val _zoomLevel = MutableStateFlow(2.0)
    val zoomLevel: StateFlow<Double> = _zoomLevel.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _selectedNodeId = MutableStateFlow<String?>(null)
    val selectedNodeId: StateFlow<String?> = _selectedNodeId.asStateFlow()

    val treeNodes: StateFlow<List<com.kaimera.tablet.core.ui.components.TreeNode>> = 
        combine(mapsRepository.allCategories, mapsRepository.allPlaces) { categories, places ->
            val rootNodes = mutableListOf<com.kaimera.tablet.core.ui.components.TreeNode>()
            
            // Favorites Node (All places)
            rootNodes.add(
                com.kaimera.tablet.core.ui.components.TreeNode(
                    id = "favorites",
                    label = "Favorites",
                    icon = androidx.compose.material.icons.Icons.Default.Favorite,
                    children = places.map { place ->
                        com.kaimera.tablet.core.ui.components.TreeNode(
                            id = "place_${place.id}",
                            label = place.name,
                            icon = androidx.compose.material.icons.Icons.Default.Place
                        )
                    }
                )
            )
            
            // Recent Node (Mock for now)
            rootNodes.add(
                com.kaimera.tablet.core.ui.components.TreeNode(
                    id = "recent",
                    label = "Recent",
                    icon = androidx.compose.material.icons.Icons.Default.History
                )
            )

            rootNodes
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.mapsLastLat.first().let { _centerLat.value = it }
            repository.mapsLastLon.first().let { _centerLon.value = it }
            repository.mapsLastZoom.first().let { _zoomLevel.value = it }
        }
    }

    fun updateMapState(lat: Double, lon: Double, zoom: Double) {
        _centerLat.value = lat
        _centerLon.value = lon
        _zoomLevel.value = zoom
        
        viewModelScope.launch {
            repository.setMapsLastState(lat, lon, zoom)
        }
    }

    fun setFollowing(following: Boolean) {
        _isFollowing.value = following
    }

    fun selectNode(nodeId: String) {
        _selectedNodeId.value = nodeId
        if (nodeId.startsWith("place_")) {
            val placeId = nodeId.removePrefix("place_").toLongOrNull()
            if (placeId != null) {
                viewModelScope.launch {
                    mapsRepository.allPlaces.first().find { it.id == placeId }?.let { place ->
                        _centerLat.value = place.latitude
                        _centerLon.value = place.longitude
                        _zoomLevel.value = 15.0 // Zoom in on the place
                        setFollowing(false)
                    }
                }
            }
        }
    }

    fun savePlace(name: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            mapsRepository.savePlace(
                com.kaimera.tablet.features.maps.data.entities.SavedPlace(
                    name = name,
                    latitude = lat,
                    longitude = lon
                )
            )
        }
    }
}
