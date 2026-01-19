package com.kaimera.tablet.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kaimera.tablet.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(onNavigate: (String) -> Unit = {}) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Settings",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Common Section
            SettingsSection(title = "Common") {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val date = Date(BuildConfig.BUILD_TIMESTAMP)
                Text(
                    text = "Build Timestamp: ${dateFormat.format(date)}",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Applets Section
            SettingsSection(title = "Applets") {
                AppletSetting(
                    name = "Camera",
                    description = "Configure resolution, grid, and timer",
                    onClick = { onNavigate("camera_settings") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppletSetting(
                    name = "Files",
                    description = "File manager settings placeholder",
                    onClick = { /* TODO */ }
                )
            }
        }
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
fun AppletSetting(name: String, description: String, onClick: () -> Unit) {
    Column(modifier = Modifier.clickable(onClick = onClick).fillMaxWidth().padding(vertical=8.dp)) {
        Text(text = name, style = MaterialTheme.typography.titleMedium)
        Text(text = description, style = MaterialTheme.typography.bodyMedium)
    }
}
