package com.kaimera.tablet.features.camera

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kaimera.tablet.core.data.UserPreferencesRepository
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.camera.core.CameraSelector
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun CameraSettings(
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPreferences = viewModel.userPreferences

    val gridRows by userPreferences.gridRows.collectAsState(initial = 0)
    val gridCols by userPreferences.gridCols.collectAsState(initial = 0)
    val timerSeconds by userPreferences.timerSeconds.collectAsState(initial = 0)
    
    val photoResolutionTier by userPreferences.photoResolutionTier.collectAsState(initial = 1)
    val videoResolutionTier by userPreferences.videoResolutionTier.collectAsState(initial = 1)
    val videoFps by userPreferences.videoFps.collectAsState(initial = 30)

    val jpegQuality by userPreferences.jpegQuality.collectAsState(initial = 95)
    val circleRadiusPercent by userPreferences.circleRadiusPercent.collectAsState(initial = 20)
    val captureMode by userPreferences.captureMode.collectAsState(initial = 1)

    // State for supported video qualities
    var supportedQualities by remember { mutableStateOf<List<Quality>>(emptyList()) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            if (cameraProvider.hasCamera(cameraSelector)) {
                try {
                    val cameraInfos = cameraProvider.availableCameraInfos
                    val cameraInfo = cameraInfos.firstOrNull { 
                        try {
                            // Filter requires a list of CameraInfo, but implementation details vary.
                            // Simpler check: lens facing
                            (it as? androidx.camera.core.CameraInfo)?.let { info ->
                                // This is a bit hacky without exact selector matching logic available in public API easily
                                // relying on the first back camera found
                                // Actually, use selector to filter
                                val filtered = cameraSelector.filter(cameraInfos)
                                filtered.contains(it)
                            } == true
                        } catch (e: Exception) { false }
                    } ?: cameraInfos.firstOrNull() // Fallback
                    
                    cameraInfo?.let {
                        supportedQualities = QualitySelector.getSupportedQualities(it)
                    }
                } catch (e: Exception) {
                    // Handle error
                }
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // PHOTO SETTINGS
            Text(text = "Photo Settings", style = MaterialTheme.typography.titleLarge)
            
            Text(text = "Resolution", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                val tiers = listOf("HD (720p)", "FHD (1080p)", "Max")
                tiers.forEachIndexed { index, label ->
                    val isSelected = photoResolutionTier == index
                    if (isSelected) {
                        Button(onClick = {}) { Text(label) }
                    } else {
                        OutlinedButton(onClick = { scope.launch { userPreferences.setPhotoResolutionTier(index) } }) { Text(label) }
                    }
                    if (index < tiers.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            SettingsSlider(
                label = "JPEG Quality: $jpegQuality",
                value = jpegQuality,
                range = 1f..100f,
                onValueChange = { scope.launch { userPreferences.setJpegQuality(it.roundToInt()) } }
            )
            
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(
                    checked = captureMode == 1,
                    onCheckedChange = { isChecked ->
                        scope.launch { userPreferences.setCaptureMode(if (isChecked) 1 else 0) }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = "Prioritize Quality", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = if (captureMode == 1) "Maximizing quality" else "Minimizing latency",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // VIDEO SETTINGS
            Text(text = "Video Settings", style = MaterialTheme.typography.titleLarge)
            
            Text(text = "Resolution", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                val tiers = listOf(
                    Triple(0, "HD (720p)", Quality.HD),
                    Triple(1, "FHD (1080p)", Quality.FHD),
                    Triple(2, "4K", Quality.UHD)
                )
                tiers.forEachIndexed { index, (tierIndex, label, quality) ->
                    val isSupported = supportedQualities.contains(quality) || supportedQualities.isEmpty() // Assume supported if list empty (loading or error)
                    val isSelected = videoResolutionTier == tierIndex
                    
                    if (isSelected) {
                        Button(onClick = {}) { Text(label) }
                    } else {
                        OutlinedButton(
                            onClick = { scope.launch { userPreferences.setVideoResolutionTier(tierIndex) } },
                            enabled = isSupported
                        ) { 
                            Text(if (isSupported) label else "$label (N/A)") 
                        }
                    }
                    if (index < tiers.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }

            Text(text = "Frame Rate", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                val fpsOptions = listOf(30, 60)
                fpsOptions.forEach { fps ->
                    val isSelected = videoFps == fps
                    if (isSelected) {
                        Button(onClick = {}) { Text("${fps} FPS") }
                    } else {
                        OutlinedButton(onClick = { scope.launch { userPreferences.setVideoFps(fps) } }) { Text("${fps} FPS") }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            androidx.compose.material3.Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // OVERLAY SETTINGS
            Text(text = "Overlays & General", style = MaterialTheme.typography.titleLarge)
            
            SettingsSlider(
                label = "Grid Rows: $gridRows",
                value = gridRows,
                range = 0f..10f,
                steps = 9,
                onValueChange = { scope.launch { userPreferences.setGridRows(it.roundToInt()) } }
            )
            SettingsSlider(
                label = "Grid Cols: $gridCols",
                value = gridCols,
                range = 0f..10f,
                steps = 9,
                onValueChange = { scope.launch { userPreferences.setGridCols(it.roundToInt()) } }
            )

            SettingsSlider(
                label = "Delay (Seconds): $timerSeconds",
                value = timerSeconds,
                range = 0f..100f,
                onValueChange = { scope.launch { userPreferences.setTimerSeconds(it.roundToInt()) } }
            )

            SettingsSlider(
                label = "Center Circle Radius: ${circleRadiusPercent}%",
                value = circleRadiusPercent,
                range = 0f..100f,
                onValueChange = { scope.launch { userPreferences.setCircleRadiusPercent(it.roundToInt()) } }
            )
        }
    }
}

@Composable
fun SettingsSlider(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = value.toFloat(),
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps
        )
    }
}
