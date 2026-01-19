package com.kaimera.tablet.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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

    Scaffold { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            // Grid Settings
            SettingsSlider(
                label = "Horizontal Lines (Rows): $gridRows",
                value = gridRows,
                range = 0f..10f,
                onValueChange = { scope.launch { userPreferences.setGridRows(it.roundToInt()) } }
            )
            SettingsSlider(
                label = "Vertical Lines (Columns): $gridCols",
                value = gridCols,
                range = 0f..10f,
                onValueChange = { scope.launch { userPreferences.setGridCols(it.roundToInt()) } }
            )

            // Timer Settings
            SettingsSlider(
                label = "Delay (Seconds): $timerSeconds",
                value = timerSeconds,
                range = 0f..100f,
                onValueChange = { scope.launch { userPreferences.setTimerSeconds(it.roundToInt()) } }
            )
        }
    }
}

@Composable
fun SettingsSlider(
    label: String,
    value: Int,
    range: ClosedFloatingPointRange<Float>,
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
            valueRange = range
        )
    }
}
