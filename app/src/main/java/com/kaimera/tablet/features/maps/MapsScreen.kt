package com.kaimera.tablet.features.maps

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kaimera.tablet.core.ui.components.TreePanel
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun MapsScreen(
    onBack: () -> Unit,
    viewModel: MapsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val centerLat by viewModel.centerLat.collectAsStateWithLifecycle()
    val centerLon by viewModel.centerLon.collectAsStateWithLifecycle()
    val zoomLevel by viewModel.zoomLevel.collectAsStateWithLifecycle()
    val isFollowing by viewModel.isFollowing.collectAsStateWithLifecycle()
    val treeNodes by viewModel.treeNodes.collectAsStateWithLifecycle()
    val selectedNodeId by viewModel.selectedNodeId.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var pendingPoint by remember { mutableStateOf<GeoPoint?>(null) }
    var placeName by remember { mutableStateOf("") }

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // Configure osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    // Lifecycle Management
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapView?.onResume()
                    if (locationPermissionState.allPermissionsGranted) {
                        locationOverlay?.enableMyLocation()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    mapView?.onPause()
                    locationOverlay?.disableMyLocation()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Place") },
            text = {
                TextField(
                    value = placeName,
                    onValueChange = { placeName = it },
                    placeholder = { Text("Enter place name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingPoint?.let { 
                            viewModel.savePlace(placeName, it.latitude, it.longitude)
                        }
                        showSaveDialog = false
                        placeName = ""
                    },
                    enabled = placeName.isNotBlank()
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Maps") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (locationPermissionState.allPermissionsGranted) {
                        viewModel.setFollowing(!isFollowing)
                        if (!isFollowing) {
                            locationOverlay?.enableFollowLocation()
                        } else {
                            locationOverlay?.disableFollowLocation()
                        }
                    } else {
                        locationPermissionState.launchMultiplePermissionRequest()
                    }
                },
                containerColor = if (isFollowing) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Icon(
                    imageVector = if (isFollowing) Icons.Default.Navigation else Icons.Default.MyLocation,
                    contentDescription = "My Location",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TreePanel(
                nodes = treeNodes,
                selectedNodeId = selectedNodeId,
                onNodeSelected = { viewModel.selectNode(it.id) },
                modifier = Modifier.width(260.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                            
                            controller.setZoom(zoomLevel)
                            controller.setCenter(GeoPoint(centerLat, centerLon))

                            // Location Overlay
                            val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                            if (locationPermissionState.allPermissionsGranted) {
                                myLocationOverlay.enableMyLocation()
                            }
                            overlays.add(myLocationOverlay)
                            locationOverlay = myLocationOverlay

                            // Compass Overlay
                            val compassOverlay = CompassOverlay(ctx, InternalCompassOrientationProvider(ctx), this)
                            compassOverlay.enableCompass()
                            overlays.add(compassOverlay)

                            // Map Events Overlay
                            val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean = false
                                override fun longPressHelper(p: GeoPoint?): Boolean {
                                    pendingPoint = p
                                    showSaveDialog = true
                                    return true
                                }
                            })
                            overlays.add(eventsOverlay)

                            addMapListener(object : org.osmdroid.events.MapListener {
                                override fun onScroll(event: org.osmdroid.events.ScrollEvent?): Boolean {
                                    if (isFollowing && event?.source?.isAnimating == false) {
                                         viewModel.setFollowing(false)
                                         locationOverlay?.disableFollowLocation()
                                    }
                                    syncMapState(this@apply, viewModel)
                                    return true
                                }
                                override fun onZoom(event: org.osmdroid.events.ZoomEvent?): Boolean {
                                    syncMapState(this@apply, viewModel)
                                    return true
                                }
                            })

                            mapView = this
                        }
                    },
                    update = { view ->
                        // If following is false and view center is different from state, update view
                        val center = view.mapCenter as GeoPoint
                        if (Math.abs(center.latitude - centerLat) > 0.000001 || 
                            Math.abs(center.longitude - centerLon) > 0.000001 ||
                            Math.abs(view.zoomLevelDouble - zoomLevel) > 0.1) {
                            
                            if (!isFollowing) {
                                view.controller.animateTo(GeoPoint(centerLat, centerLon))
                                view.controller.setZoom(zoomLevel)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Zoom Controls
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(
                        onClick = { mapView?.controller?.zoomIn() },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Zoom In")
                    }
                    IconButton(
                        onClick = { mapView?.controller?.zoomOut() },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                    }
                }

                if (!locationPermissionState.allPermissionsGranted) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Location access needed.",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = { locationPermissionState.launchMultiplePermissionRequest() }) {
                                Text("Enable")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun syncMapState(mapView: MapView, viewModel: MapsViewModel) {
    val center = mapView.mapCenter as GeoPoint
    viewModel.updateMapState(center.latitude, center.longitude, mapView.zoomLevelDouble)
}
