package com.kaimera.tablet.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.kaimera.tablet.data.UserPreferencesRepository
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CameraSettings() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val userPreferences = UserPreferencesRepository(context)

    val gridRows by userPreferences.gridRows.collectAsState(initial = 0)
    val gridCols by userPreferences.gridCols.collectAsState(initial = 0)
    val timerSeconds by userPreferences.timerSeconds.collectAsState(initial = 0)
    val resolutionTier by userPreferences.resolutionTier.collectAsState(initial = 0)

    val jpegQuality by userPreferences.jpegQuality.collectAsState(initial = 95)
    val circleRadiusPercent by userPreferences.circleRadiusPercent.collectAsState(initial = 20)
    val captureMode by userPreferences.captureMode.collectAsState(initial = 1)

    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            // Grid Settings
            SettingsSlider(
                label = "Horizontal Lines (Rows): $gridRows",
                value = gridRows,
                range = 0f..10f,
                steps = 9,
                onValueChange = { scope.launch { userPreferences.setGridRows(it.roundToInt()) } }
            )
            SettingsSlider(
                label = "Vertical Lines (Columns): $gridCols",
                value = gridCols,
                range = 0f..10f,
                steps = 9,
                onValueChange = { scope.launch { userPreferences.setGridCols(it.roundToInt()) } }
            )

            // Timer Settings
            SettingsSlider(
                label = "Delay (Seconds): $timerSeconds",
                value = timerSeconds,
                range = 0f..100f,
                onValueChange = { scope.launch { userPreferences.setTimerSeconds(it.roundToInt()) } }
            )

            // Overlay Settings
            SettingsSlider(
                label = "Center Circle Radius: ${circleRadiusPercent}%",
                value = circleRadiusPercent,
                range = 0f..100f,
                onValueChange = { scope.launch { userPreferences.setCircleRadiusPercent(it.roundToInt()) } }
            )

            // Image Settings
            // Image Settings
            Text(text = "Resolution", style = MaterialTheme.typography.bodyMedium)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                val tiers = listOf("HD (720p)", "FHD (1080p)", "Max")
                tiers.forEachIndexed { index, label ->
                    val isSelected = resolutionTier == index
                    if (isSelected) {
                        Button(onClick = {}) { Text(label) }
                    } else {
                        OutlinedButton(onClick = { scope.launch { userPreferences.setResolutionTier(index) } }) { Text(label) }
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

            // Capture Mode (Toggle First for Landscape UX)
            Row(modifier = Modifier.padding(vertical = 16.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Switch(
                    checked = captureMode == 1,
                    onCheckedChange = { isChecked ->
                        scope.launch { userPreferences.setCaptureMode(if (isChecked) 1 else 0) }
                    }
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Prioritize Quality", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (captureMode == 1) "Maximizing image quality (slower)" else "Minimizing latency (faster)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
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
