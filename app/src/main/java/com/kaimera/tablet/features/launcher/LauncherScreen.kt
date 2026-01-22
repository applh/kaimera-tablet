package com.kaimera.tablet.features.launcher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class AppletItem(
    val name: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@Composable
fun LauncherScreen(onAppletSelected: (String) -> Unit) {
    val applets = listOf(
        AppletItem("Camera", Icons.Default.PhotoCamera, "camera", Color(0xFFD81B60)),     // Pink
        AppletItem("Files", Icons.Default.Folder, "files", Color(0xFFFB8C00)),           // Orange
        AppletItem("Browser", Icons.Default.Language, "browser", Color(0xFF1E88E5)),     // Blue
        AppletItem("Notes", Icons.Default.EditNote, "notes", Color(0xFFFBC02D)),         // Yellow
        AppletItem("Downloads", Icons.Default.Download, "downloads", Color(0xFF43A047)), // Green
        AppletItem("Settings", Icons.Default.Settings, "settings", Color(0xFF757575)),   // Grey
        AppletItem("Calendar", Icons.Default.Event, "calendar", Color(0xFFE53935)),      // Red
        AppletItem("Projects", Icons.AutoMirrored.Filled.Assignment, "projects", Color(0xFF00ACC1)) // Cyan
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, start = 16.dp, end = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Welcome to Kaimera",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            HexagonalGrid(
                items = applets,
                columns = 3,
                itemSize = 140.dp, // Slightly larger for better touch targets
                spacing = 16.dp
            ) { applet ->
                LauncherIcon(
                    name = applet.name,
                    icon = applet.icon,
                    color = applet.color,
                    onClick = { onAppletSelected(applet.route) }
                )
            }
        }
    }
}

@Composable
fun LauncherIcon(name: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Card(
            modifier = Modifier.size(100.dp), // Adjust size relative to grid cell
            shape = HexagonShape(),
            colors = CardDefaults.cardColors(
                containerColor = color
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = name,
                    modifier = Modifier.size(48.dp),
                    tint = Color.White // Ensure good contrast with colored backgrounds
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}
