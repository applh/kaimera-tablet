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
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.kaimera.tablet.ui.MediaViewerScreen
import coil.ImageLoader
import coil.decode.VideoFrameDecoder
import coil.Coil

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
                                onNavigate = { route ->
                                    navController.navigate(route)
                                }
                            )
                        }
                        composable("files") {
                            FilesScreen(
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
                            com.kaimera.tablet.ui.CameraSettings()
                        }
                    }
                }
            }
        }
    }
}
