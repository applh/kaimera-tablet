package com.kaimera.tablet.features.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.camera.extensions.ExtensionMode
import kotlin.math.roundToInt

import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.vector.ImageVector

// --- Priority 4: TOP BAR (Status & Toggles) ---
@Composable
fun CockpitTopBar(
    flashModePref: Int,
    torchEnabled: Boolean,
    aiSceneDetection: Boolean,
    supportedExtensions: List<Int>,
    extensionMode: Int,
    cameraMode: Int,
    timelapseMode: Boolean,
    scanQrCodes: Boolean,
    onFlashModeChange: (Int) -> Unit,
    onTorchChange: (Boolean) -> Unit,
    onAiSceneDetectionChange: (Boolean) -> Unit,
    onExtensionModeChange: (Int) -> Unit,
    onTimelapseModeChange: (Boolean) -> Unit,
    onScanQrCodesChange: (Boolean) -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(Color.Black.copy(alpha = 0.4f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        // 0. Menu
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Default.Menu, "Menu", tint = Color.White)
        }
        
        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

        // Flash
        IconButton(onClick = { onFlashModeChange((flashModePref + 1) % 3) }) {
            Icon(
                when (flashModePref) {
                    1 -> Icons.Filled.FlashOn; 2 -> Icons.Filled.FlashOff; else -> Icons.Filled.FlashAuto
                }, "Flash", tint = Color.White
            )
        }
        
        // Torch
        IconButton(onClick = { onTorchChange(!torchEnabled) }) {
            Icon(
                if (torchEnabled) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff,
                "Torch",
                tint = if (torchEnabled) Color.Yellow else Color.White
            )
        }
        
        // AI
        IconButton(onClick = { onAiSceneDetectionChange(!aiSceneDetection) }) {
            Icon(Icons.Filled.Psychology, "AI", tint = if (aiSceneDetection) Color.Magenta else Color.White)
        }

        // Extensions
        if (supportedExtensions.isNotEmpty() && cameraMode == 0) {
            IconButton(onClick = {
                val idx = supportedExtensions.indexOf(extensionMode)
                val next = if (idx < supportedExtensions.size - 1) supportedExtensions[idx + 1] else ExtensionMode.NONE
                onExtensionModeChange(next)
            }) {
                Icon(
                    Icons.Filled.AutoAwesome,
                    "Ext",
                    tint = if (extensionMode == ExtensionMode.NONE) Color.White else Color.Yellow
                )
            }
        }

        // Timelapse (Video Mode only)
        if (cameraMode == 1) {
            IconButton(onClick = { onTimelapseModeChange(!timelapseMode) }) {
                Icon(Icons.Filled.Timelapse, "Timelapse", tint = if (timelapseMode) Color.Red else Color.White)
            }
        }

        // QR
            IconButton(onClick = { onScanQrCodesChange(!scanQrCodes) }) {
                Icon(Icons.Filled.QrCodeScanner, "QR", tint = if (scanQrCodes) Color.Cyan else Color.White)
            }
        }
        Spacer(Modifier.width(8.dp))
    }
}

// --- Priority 1: BOTTOM BAR (Primary Actions) ---
@Composable
fun CockpitBottomBar(
    cameraMode: Int,
    isRecording: Boolean,
    isPaused: Boolean,
    timerSeconds: Int,
    isTimerRunning: Boolean,
    timelapseMode: Boolean,
    timelapseFrames: Int,
    recordingDurationNanos: Long,
    lastImageUri: android.net.Uri?,
    lensFacing: Int,
    isProMode: Boolean,
    onCapture: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onNavigateToGallery: () -> Unit,
    onCameraModeChange: (Int) -> Unit,
    onCameraLensChange: () -> Unit,
    onProModeToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp) // Reduced from 100
            .background(Color.Black.copy(alpha = 0.6f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // 1. Lens Switch
        IconButton(onClick = onCameraLensChange) {
             Icon(Icons.Filled.Cameraswitch, "Switch Lens", tint = Color.White)
        }
        
        // 2. Mode Switcher (Compact)
        Row(
            modifier = Modifier
                .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(2.dp), // Reduced from 4
            horizontalArrangement = Arrangement.spacedBy(4.dp) // Reduced from 8
        ) {
            CompactModeButton(Icons.Filled.PhotoCamera, cameraMode == 0) { onCameraModeChange(0) }
            CompactModeButton(Icons.Filled.Videocam, cameraMode == 1) { onCameraModeChange(1) }
            CompactModeButton(Icons.Filled.Settings, isProMode) { onProModeToggle() }
        }

        // 3. Shutter (Center)
        ShutterButton(
            cameraMode, isRecording, isPaused, timerSeconds, isTimerRunning, timelapseMode, timelapseFrames, recordingDurationNanos,
            onCapture, onPauseRecording, onResumeRecording,
            modifier = Modifier.size(72.dp) // Slightly smaller shutter
        )

        // 4. Gallery
        GalleryButton(lastImageUri, onNavigateToGallery)
    }
}

// --- Priority 2: RIGHT SIDEBAR (Zoom) ---
@Composable
fun CockpitSideBarRight(
    zoomRatio: Float,
    maxZoomRatio: Float,
    onZoomChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(64.dp) // Reduced from 80
            .background(Color.Black.copy(alpha = 0.3f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Filled.ZoomIn, "Zoom In", tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16
        
        // Vertical Slider for Zoom
        Box(
            modifier = Modifier.weight(1f).width(40.dp),
            contentAlignment = Alignment.Center
        ) {
             Slider(
                 value = zoomRatio,
                 onValueChange = onZoomChange,
                 valueRange = 1f..maxZoomRatio,
                 modifier = Modifier
                     .graphicsLayer { rotationZ = -90f }
                     .layout { measurable, constraints ->
                         val placeable = measurable.measure(
                             Constraints(
                                 minWidth = constraints.maxHeight,
                                 maxWidth = constraints.maxHeight,
                                 minHeight = constraints.minWidth,
                                 maxHeight = constraints.maxWidth
                             )
                         )
                         layout(placeable.height, placeable.width) {
                             placeable.place( -(placeable.width - placeable.height) / 2, (placeable.width - placeable.height) / 2 )
                         }
                     }
                     .width(300.dp), // Adjust length of slider track
                 colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
             )
        }
        
        Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16
        Icon(Icons.Filled.ZoomOut, "Zoom Out", tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

// --- Priority 3: LEFT SIDEBAR (Pro Controls - Exposure/Focus) ---
@Composable
fun CockpitSideBarLeft(
    exposureIndex: Int,
    exposureRange: android.util.Range<Int>,
    exposureStep: android.util.Rational,
    awbMode: Int,
    onExposureChange: (Int) -> Unit,
    onAwbModeChange: (Int) -> Unit
) {
    // Add import or use fully qualified name for CaptureRequest if not already imported
    // CaptureRequest is usually android.hardware.camera2.CaptureRequest
    
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(64.dp) // Reduced from 80
            .background(Color.Black.copy(alpha = 0.3f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // AWB Selector (Top of Left Sidebar)
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp), // Reduced from 4
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 4.dp) // Reduced from 8
        ) {
            val modes = listOf(
                 android.hardware.camera2.CaptureRequest.CONTROL_AWB_MODE_AUTO to "A",
                 android.hardware.camera2.CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT to "Sun",
                 android.hardware.camera2.CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT to "Cld",
                 android.hardware.camera2.CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT to "Flu",
                 android.hardware.camera2.CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT to "Inc"
             )
             for ((mode, label) in modes) {
                 Box(
                     modifier = Modifier
                         .size(32.dp)
                         .clip(CircleShape)
                         .background(if (awbMode == mode) Color.Yellow.copy(alpha=0.4f) else Color.Transparent)
                         .border(1.dp, if (awbMode == mode) Color.Yellow else Color.White.copy(alpha=0.3f), CircleShape)
                         .clickable { onAwbModeChange(mode) },
                     contentAlignment = Alignment.Center
                 ) {
                     Text(label, color = if (awbMode == mode) Color.Yellow else Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                 }
             }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Reduced from 16

        Text(
            text = "EV",
            color = Color.Yellow,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
        Text(
             if (exposureStep.numerator != 0) "%.1f".format(exposureIndex * exposureStep.numerator.toFloat() / exposureStep.denominator) else "0.0",
             color = Color.White,
             fontSize = 10.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        
        // Vertical Slider for Exposure
        Box(
            modifier = Modifier.weight(1f).width(40.dp),
            contentAlignment = Alignment.Center
        ) {
             Slider(
                 value = exposureIndex.toFloat(),
                 onValueChange = { onExposureChange(it.roundToInt()) },
                 valueRange = exposureRange.lower.toFloat()..exposureRange.upper.toFloat(),
                 modifier = Modifier
                     .graphicsLayer { rotationZ = -90f }
                     .layout { measurable, constraints ->
                         val placeable = measurable.measure(
                             Constraints(
                                 minWidth = constraints.maxHeight,
                                 maxWidth = constraints.maxHeight,
                                 minHeight = constraints.minWidth,
                                 maxHeight = constraints.maxWidth
                             )
                         )
                         layout(placeable.height, placeable.width) {
                             placeable.place( -(placeable.width - placeable.height) / 2, (placeable.width - placeable.height) / 2 )
                         }
                     }
                     .width(250.dp),
                 colors = SliderDefaults.colors(thumbColor = Color.Yellow, activeTrackColor = Color.Yellow)
             )
        }
    }
}

@Composable
fun ShutterButton(
    cameraMode: Int,
    isRecording: Boolean,
    isPaused: Boolean,
    timerSeconds: Int,
    isTimerRunning: Boolean,
    timelapseMode: Boolean,
    timelapseFrames: Int,
    recordingDurationNanos: Long,
    onCapture: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    modifier: Modifier = Modifier.size(80.dp)
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onCapture,
            modifier = modifier
        ) {
            Icon(
                imageVector = when {
                    cameraMode == 1 && isRecording -> Icons.Filled.Stop
                    cameraMode == 1 -> Icons.Filled.Videocam
                    else -> Icons.Filled.PhotoCamera
                },
                contentDescription = "Capture",
                tint = if (cameraMode == 1) Color.Red else Color.White,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        if (cameraMode == 1 && isRecording) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = { if (isPaused) onResumeRecording() else onPauseRecording() },
                    modifier = Modifier.size(32.dp).background(Color.White.copy(alpha=0.3f), CircleShape)
                ) {
                    Icon(if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause, if (isPaused) "Resume" else "Pause", tint = Color.White, modifier = Modifier.size(20.dp))
                }
                Text(
                    if (timelapseMode) "Frames: $timelapseFrames" else formatDuration(recordingDurationNanos),
                    color = if (isPaused) Color.Yellow else Color.Red,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun GalleryButton(lastImageUri: android.net.Uri?, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(40.dp) // Reduced from 50
            .border(1.5.dp, Color.White, CircleShape)
            .background(Color.DarkGray, CircleShape)
    ) {
        if (lastImageUri != null) {
            AsyncImage(
                model = lastImageUri,
                contentDescription = "Gallery",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }
    }
}


@Composable
fun CompactModeButton(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(4.dp) // Reduced from 8
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(18.dp)) // Reduced from 20
    }
}

// Helper
private fun formatDuration(nanos: Long): String {
    val seconds = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(nanos)
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
