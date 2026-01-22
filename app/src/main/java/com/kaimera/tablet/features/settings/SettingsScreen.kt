package com.kaimera.tablet.features.settings

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.unit.dp

import androidx.compose.material3.Switch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import com.kaimera.tablet.core.data.UserPreferencesRepository
import kotlinx.coroutines.launch

import androidx.hilt.navigation.compose.hiltViewModel
import com.kaimera.tablet.core.ui.components.TreeNode
import com.kaimera.tablet.core.ui.components.TreePanel
import com.kaimera.tablet.core.ui.components.NavDrawerTreePanel
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu

import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.NoteAlt
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.kaimera.tablet.features.camera.CameraSettings

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val scope = rememberCoroutineScope()
    val userPreferences = viewModel.userPreferences
    val isDebugMode by userPreferences.isDebugMode.collectAsState(initial = false)

    val treeNodes = remember {
        listOf(
            TreeNode("home", "Home", Icons.Default.Home),
            TreeNode("applets", "Applets", Icons.Default.Apps, children = listOf(
                TreeNode("camera", "Camera", Icons.Default.CameraAlt),
                TreeNode("browser", "Browser", Icons.Default.Language),
                TreeNode("downloads", "Downloads", Icons.Default.Download),
                TreeNode("files", "Files", Icons.Default.Folder),
                TreeNode("notes", "Notes", Icons.Default.NoteAlt)
            ))
        )
    }
    var selectedNodeId by remember { mutableStateOf("home") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    NavDrawerTreePanel(
        drawerState = drawerState,
        title = "Settings",
        onHomeClick = onBack,
        nodes = treeNodes,
        selectedNodeId = selectedNodeId,

        onNodeSelected = { selectedNodeId = it.id }
    ) {
        com.kaimera.tablet.core.ui.components.AppletScaffold(
            title = "Settings",
            onMenuClick = { scope.launch { drawerState.open() } }
        ) { paddingValues ->

            Box(modifier = Modifier.fillMaxSize()) {

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    when (selectedNodeId) {
                        "home" -> GeneralSettings(isDebugMode) {
                            scope.launch { userPreferences.setDebugMode(it) }
                        }
                        "camera" -> CameraSettings()
                        else -> PlaceholderSettings(selectedNodeId.replaceFirstChar { it.uppercase() })
                    }
                }
            }
        }
    }
}

@Composable
fun GeneralSettings(isDebugMode: Boolean, onDebugModeChange: (Boolean) -> Unit) {
    Column {
        Text(
            text = "General Settings",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        SettingsSection(title = "System") {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = Date(BuildConfig.BUILD_TIMESTAMP)
            Text(
                text = "Build Timestamp: ${dateFormat.format(date)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Debug Mode", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isDebugMode,
                    onCheckedChange = onDebugModeChange
                )
            }
        }
    }
}

@Composable
fun PlaceholderSettings(name: String) {
    Column {
        Text(
            text = "$name Settings",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        Text(
            text = "Settings for the $name applet are coming soon.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
