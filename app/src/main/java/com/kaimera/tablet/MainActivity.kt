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
import com.kaimera.tablet.core.ui.theme.KaimeraTabletTheme

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kaimera.tablet.features.camera.CameraScreen
import com.kaimera.tablet.features.launcher.LauncherScreen
import com.kaimera.tablet.features.settings.SettingsScreen
import com.kaimera.tablet.features.files.FilesScreen
import com.kaimera.tablet.features.files.viewer.MediaViewerScreen
import com.kaimera.tablet.features.browser.BrowserScreen
import com.kaimera.tablet.features.notes.NotesScreen
import com.kaimera.tablet.features.downloads.DownloadsScreen
import com.kaimera.tablet.features.calendar.CalendarScreen
import com.kaimera.tablet.features.projects.ProjectsScreen
import com.kaimera.tablet.features.maps.MapsScreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.navArgument
import androidx.navigation.NavType
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.Coil

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Coil for Video Thumbnails
        val imageLoader = ImageLoader.Builder(this)
            .components {
                add(VideoFrameDecoder.Factory())
            }
            .build()
        Coil.setImageLoader(imageLoader)

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
                                onBack = { navController.popBackStack() },
                                onNavigate = { route ->
                                    navController.navigate(route)
                                }
                            )
                        }
                        composable("files") {
                            FilesScreen(
                                onBack = { navController.popBackStack() },
                                onFileOpen = { media ->
                                    val encodedUri = java.net.URLEncoder.encode(media.uri.toString(), "UTF-8")
                                    navController.navigate("viewer?uri=$encodedUri&isVideo=${media.isVideo}")
                                }
                            )
                        }
                        composable(
                            route = "viewer?uri={uri}&isVideo={isVideo}",
                            arguments = listOf(
                                navArgument("uri") { type = NavType.StringType },
                                navArgument("isVideo") { type = NavType.BoolType }
                            )
                        ) { backStackEntry ->
                            val uriString = backStackEntry.arguments?.getString("uri")
                            val isVideo = backStackEntry.arguments?.getBoolean("isVideo") ?: false
                            if (uriString != null) {
                                MediaViewerScreen(
                                    uri = android.net.Uri.parse(uriString),
                                    isVideo = isVideo,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                        composable("camera_settings") {
                            com.kaimera.tablet.features.camera.CameraSettings()
                        }
                        composable("browser") {
                            BrowserScreen(onBack = { navController.popBackStack() })
                        }
                        composable("notes") {
                            NotesScreen(onBack = { navController.popBackStack() })
                        }
                        composable("downloads") {
                            DownloadsScreen(onBack = { navController.popBackStack() })
                        }
                        composable("calendar") {
                            CalendarScreen(onBack = { navController.popBackStack() })
                        }
                        composable("projects") {
                            ProjectsScreen(onBack = { navController.popBackStack() })
                        }
                        composable("maps") {
                            MapsScreen(onBack = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}
