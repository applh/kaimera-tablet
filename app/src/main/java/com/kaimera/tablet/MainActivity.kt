package com.kaimera.tablet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.kaimera.tablet.ui.theme.KaimeraTabletTheme

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kaimera.tablet.ui.CameraScreen
import com.kaimera.tablet.ui.LauncherScreen
import com.kaimera.tablet.ui.SettingsScreen
import com.kaimera.tablet.files.FilesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KaimeraTabletTheme {
                val navController = rememberNavController()
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(navController = navController, startDestination = "launcher") {
                        composable("launcher") {
                            LauncherScreen(
                                onAppletSelected = { applet ->
                                    navController.navigate(applet)
                                }
                            )
                        }
                        composable("camera") {
                            CameraScreen(
                                onNavigateToGallery = {
                                    navController.navigate("files")
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigate = { route ->
                                    navController.navigate(route)
                                }
                            )
                        }
                        composable("files") {
                            FilesScreen()
                        }
                        composable("camera_settings") {
                            com.kaimera.tablet.ui.CameraSettings()
                        }
                    }
                }
            }
        }
    }
}
