package com.kaimera.tablet.ui

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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.camera.core.CameraSelector
import android.hardware.camera2.CaptureRequest
import androidx.camera.extensions.ExtensionMode
import coil.compose.AsyncImage
import androidx.compose.ui.layout.layout
import kotlin.math.roundToInt

enum class PanelOrientation {
    Vertical,   // For Portrait/Bottom Bar (Items stacked vertically, but the bar itself is horizontal... wait)
                // Let's define: Vertical = The panel *is* a vertical bar (Sidebar).
                // Horizontal = The panel *is* a horizontal bar (Bottom bar).
    Horizontal
}

@Composable
fun CameraControlPanel(
    orientation: PanelOrientation,
    // State
    zoomRatio: Float,
    maxZoomRatio: Float,
    flashModePref: Int,
    torchEnabled: Boolean,
    aiSceneDetection: Boolean,
    supportedExtensions: List<Int>,
    extensionMode: Int,
    cameraMode: Int,
    timelapseMode: Boolean,
    scanQrCodes: Boolean,
    lensFacing: Int,
    isProMode: Boolean,
    exposureIndex: Int,
    exposureRange: android.util.Range<Int>,
    exposureStep: android.util.Rational,
    awbMode: Int,
    isRecording: Boolean,
    timerSeconds: Int,
    isTimerRunning: Boolean,
    timelapseInterval: Long,
    lastImageUri: android.net.Uri?,
    isPaused: Boolean,
    timelapseFrames: Int,
    recordingDurationNanos: Long,
    
    // Actions
    onZoomChange: (Float) -> Unit,
    onJavascriptConsoleChange: () -> Unit = {}, // Placeholder
    onTimelapseIntervalChange: (Long) -> Unit,
    onFlashModeChange: (Int) -> Unit,
    onTorchChange: (Boolean) -> Unit,
    onAiSceneDetectionChange: (Boolean) -> Unit,
    onExtensionModeChange: (Int) -> Unit,
    onTimelapseModeChange: (Boolean) -> Unit,
    onScanQrCodesChange: (Boolean) -> Unit,
    onCameraLensChange: () -> Unit,
    onProModeToggle: () -> Unit,
    onExposureChange: (Int) -> Unit,
    onAwbModeChange: (Int) -> Unit,
    onCameraModeChange: (Int) -> Unit,
    onCapture: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onNavigateToGallery: () -> Unit
) {
    // If Orientation is Vertical (Sidebar):
    // Main Container is ROW (Zoom Col | Controls Col)
    
    // If Orientation is Horizontal (BottomBar):
    // Main Container is COLUMN (Zoom Row / Controls Row)
    
    val isVerticalBar = orientation == PanelOrientation.Vertical

    val mainContainerModifier = if (isVerticalBar) {
        Modifier
            .fillMaxHeight()
            .width(300.dp) // Increased width for 4-column grid
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .padding(8.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp) // Allow expansion for Pro controls
            .wrapContentHeight()
            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(8.dp)
    }

    if (isVerticalBar) {
        Row(
            modifier = mainContainerModifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
             // 1. Zoom and Timelapse (Left Column in Sidebar)
             ZoomAndTimelapseControl(
                 isVerticalLayout = true,
                 zoomRatio = zoomRatio,
                 maxZoomRatio = maxZoomRatio,
                 onZoomChange = onZoomChange,
                 timelapseMode = timelapseMode,
                 cameraMode = cameraMode,
                 timelapseInterval = timelapseInterval,
                 onTimelapseIntervalChange = onTimelapseIntervalChange
             )
             
             // 2. Main Controls (Right Column in Sidebar)
             MainControls(
                 isVerticalLayout = true,
                 flashModePref = flashModePref,
                 torchEnabled = torchEnabled,
                 aiSceneDetection = aiSceneDetection,
                 supportedExtensions = supportedExtensions,
                 extensionMode = extensionMode,
                 cameraMode = cameraMode,
                 timelapseMode = timelapseMode,
                 scanQrCodes = scanQrCodes,
                 lensFacing = lensFacing,
                 isProMode = isProMode,
                 exposureIndex = exposureIndex,
                 exposureRange = exposureRange,
                 exposureStep = exposureStep,
                 awbMode = awbMode,
                 isRecording = isRecording,
                 timerSeconds = timerSeconds,
                 isTimerRunning = isTimerRunning,
                 lastImageUri = lastImageUri,
                 isPaused = isPaused,
                 timelapseFrames = timelapseFrames,
                 recordingDurationNanos = recordingDurationNanos,
                 onFlashModeChange = onFlashModeChange,
                 onTorchChange = onTorchChange,
                 onAiSceneDetectionChange = onAiSceneDetectionChange,
                 onExtensionModeChange = onExtensionModeChange,
                 onTimelapseModeChange = onTimelapseModeChange,
                 onScanQrCodesChange = onScanQrCodesChange,
                 onCameraLensChange = onCameraLensChange,
                 onProModeToggle = onProModeToggle,
                 onExposureChange = onExposureChange,
                 onAwbModeChange = onAwbModeChange,
                 onCameraModeChange = onCameraModeChange,
                 onCapture = onCapture,
                 onPauseRecording = onPauseRecording,
                 onResumeRecording = onResumeRecording,
                 onNavigateToGallery = onNavigateToGallery
             )
        }
    } else {
        // Horizontal Bottom Bar
        // Stack Zoom ON TOP of Controls
        Column(
             modifier = mainContainerModifier,
             verticalArrangement = Arrangement.spacedBy(8.dp),
             horizontalAlignment = Alignment.CenterHorizontally
        ) {
             // 1. Zoom and Timelapse (Top Row in Bottom Bar)
             ZoomAndTimelapseControl(
                 isVerticalLayout = false, // Horizontal layout for bottom bar
                 zoomRatio = zoomRatio,
                 maxZoomRatio = maxZoomRatio,
                 onZoomChange = onZoomChange,
                 timelapseMode = timelapseMode,
                 cameraMode = cameraMode,
                 timelapseInterval = timelapseInterval,
                 onTimelapseIntervalChange = onTimelapseIntervalChange
             )
             
             // 2. Pro Controls (Row 2 - New)
             ProControls(
                 isVerticalLayout = false,
                 isProMode = isProMode,
                 exposureIndex = exposureIndex,
                 exposureRange = exposureRange,
                 exposureStep = exposureStep,
                 awbMode = awbMode,
                 onExposureChange = onExposureChange,
                 onAwbModeChange = onAwbModeChange
             )
             
             // 3. Main Controls (Row 3 - Toggles & Shutter)
             MainControls(
                 isVerticalLayout = false,
                 flashModePref = flashModePref,
                 torchEnabled = torchEnabled,
                 aiSceneDetection = aiSceneDetection,
                 supportedExtensions = supportedExtensions,
                 extensionMode = extensionMode,
                 cameraMode = cameraMode,
                 timelapseMode = timelapseMode,
                 scanQrCodes = scanQrCodes,
                 lensFacing = lensFacing,
                 isProMode = isProMode,
                 exposureIndex = exposureIndex,
                 exposureRange = exposureRange,
                 exposureStep = exposureStep,
                 awbMode = awbMode,
                 isRecording = isRecording,
                 timerSeconds = timerSeconds,
                 isTimerRunning = isTimerRunning,
                 lastImageUri = lastImageUri,
                 isPaused = isPaused,
                 timelapseFrames = timelapseFrames,
                 recordingDurationNanos = recordingDurationNanos,
                 onFlashModeChange = onFlashModeChange,
                 onTorchChange = onTorchChange,
                 onAiSceneDetectionChange = onAiSceneDetectionChange,
                 onExtensionModeChange = onExtensionModeChange,
                 onTimelapseModeChange = onTimelapseModeChange,
                 onScanQrCodesChange = onScanQrCodesChange,
                 onCameraLensChange = onCameraLensChange,
                 onProModeToggle = onProModeToggle,
                 onExposureChange = onExposureChange,
                 onAwbModeChange = onAwbModeChange,
                 onCameraModeChange = onCameraModeChange,
                 onCapture = onCapture,
                 onPauseRecording = onPauseRecording,
                 onResumeRecording = onResumeRecording,
                 onNavigateToGallery = onNavigateToGallery
             )
        }
    }
}

@Composable
fun ZoomAndTimelapseControl(
    isVerticalLayout: Boolean,
    zoomRatio: Float,
    maxZoomRatio: Float,
    onZoomChange: (Float) -> Unit,
    timelapseMode: Boolean,
    cameraMode: Int,
    timelapseInterval: Long,
    onTimelapseIntervalChange: (Long) -> Unit
) {
    if (isVerticalLayout) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
             // Vertical Slider
             Column(
                modifier = Modifier.height(200.dp), // Limit height
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
             ) {
                 Icon(Icons.Filled.ZoomIn, "Zoom In", tint = Color.White, modifier = Modifier.size(20.dp))
                 Box(
                    modifier = Modifier
                        .width(64.dp), 
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
                             .width(160.dp), // Match containment
                         colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
                     )
                 }
                 Icon(Icons.Filled.ZoomOut, "Zoom Out", tint = Color.White, modifier = Modifier.size(20.dp))
             }

             if (timelapseMode && cameraMode == 1) {
                 Spacer(modifier = Modifier.height(24.dp))
                 // Vertical Interval Selector
                 Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.History, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                    TimelapseIntervals(timelapseInterval, onTimelapseIntervalChange)
                 }
             }
        }
    } else {
        // Horizontal Layout (Bottom Bar)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.ZoomOut, "Zoom Out", tint = Color.White, modifier = Modifier.size(20.dp))
            Slider(
                value = zoomRatio,
                onValueChange = onZoomChange,
                valueRange = 1f..maxZoomRatio,
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = Color.White)
            )
            Icon(Icons.Filled.ZoomIn, "Zoom In", tint = Color.White, modifier = Modifier.size(20.dp))
             
             if (timelapseMode && cameraMode == 1) {
                 Spacer(modifier = Modifier.width(24.dp))
                 Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                     Icon(Icons.Filled.History, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                     TimelapseIntervals(timelapseInterval, onTimelapseIntervalChange)
                 }
             }
        }
    }
}

@Composable
fun TimelapseIntervals(current: Long, onChange: (Long) -> Unit) {
    listOf(500L, 1000L, 2000L, 5000L).forEach { interval ->
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(if (current == interval) Color.Red else Color.Black.copy(alpha = 0.5f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                .clickable { onChange(interval) },
            contentAlignment = Alignment.Center
        ) {
            Text("${interval/1000f}s".replace(".0", ""), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
fun MainControls(
    isVerticalLayout: Boolean,
    flashModePref: Int,
    torchEnabled: Boolean,
    aiSceneDetection: Boolean,
    supportedExtensions: List<Int>,
    extensionMode: Int,
    cameraMode: Int,
    timelapseMode: Boolean,
    scanQrCodes: Boolean,
    lensFacing: Int,
    isProMode: Boolean,
    exposureIndex: Int,
    exposureRange: android.util.Range<Int>,
    exposureStep: android.util.Rational,
    awbMode: Int,
    isRecording: Boolean,
    timerSeconds: Int,
    isTimerRunning: Boolean,
    lastImageUri: android.net.Uri?,
    isPaused: Boolean,
    timelapseFrames: Int,
    recordingDurationNanos: Long,
    onFlashModeChange: (Int) -> Unit,
    onTorchChange: (Boolean) -> Unit,
    onAiSceneDetectionChange: (Boolean) -> Unit,
    onExtensionModeChange: (Int) -> Unit,
    onTimelapseModeChange: (Boolean) -> Unit,
    onScanQrCodesChange: (Boolean) -> Unit,
    onCameraLensChange: () -> Unit,
    onProModeToggle: () -> Unit,
    onExposureChange: (Int) -> Unit,
    onAwbModeChange: (Int) -> Unit,
    onCameraModeChange: (Int) -> Unit,
    onCapture: () -> Unit,
    onPauseRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onNavigateToGallery: () -> Unit
) {
    val modifier = if (isVerticalLayout) Modifier.fillMaxHeight() else Modifier.fillMaxWidth()
    
    // In Vertical Layout (Sidebar):
    // Toggles are Top
    // Shutter is Center
    // Gallery is Bottom (Space Between)
    
    // In Horizontal Layout (Bottom Bar):
    // Toggles are Left
    // Shutter is Center
    // Gallery is Right
    
    if (isVerticalLayout) {
        // SIDEBAR LAYOUT (Cols 2, 3, 4)
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Col 2: Toggles
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                 Toggles(
                     isVerticalLayout = true,
                     flashModePref, torchEnabled, aiSceneDetection, supportedExtensions, extensionMode, cameraMode, timelapseMode, scanQrCodes, lensFacing,
                     onFlashModeChange, onTorchChange, onAiSceneDetectionChange, onExtensionModeChange, onTimelapseModeChange, onScanQrCodesChange, onCameraLensChange
                 )
            }

            // Col 3: Pro Controls (Vertical)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                 ProControls(
                     isVerticalLayout = true,
                     isProMode = isProMode, 
                     exposureIndex = exposureIndex, 
                     exposureRange = exposureRange, 
                     exposureStep = exposureStep, 
                     awbMode = awbMode, 
                     onExposureChange = onExposureChange, 
                     onAwbModeChange = onAwbModeChange
                 )
            }

            // Col 4: Primary Actions (Mode, Shutter, Gallery)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                 ModeSwitcher(
                     isVerticalLayout = true, 
                     cameraMode = cameraMode, 
                     isProMode = isProMode, 
                     onCameraModeChange = onCameraModeChange, 
                     onProModeToggle = onProModeToggle
                 )
                 
                 ShutterButton(
                     cameraMode, isRecording, isPaused, timerSeconds, isTimerRunning, timelapseMode, timelapseFrames, recordingDurationNanos,
                     onCapture, onPauseRecording, onResumeRecording
                 )
                 
                 GalleryButton(lastImageUri, onNavigateToGallery)
            }
        }
    } else {
        // Portrait Layout (Bottom Bar)
        // Rows: Toggles, then Shutter/Gallery
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
             // Row 1: Horizontal Toggles
             Toggles(
                 isVerticalLayout = false,
                 flashModePref, torchEnabled, aiSceneDetection, supportedExtensions, extensionMode, cameraMode, timelapseMode, scanQrCodes, lensFacing,
                 onFlashModeChange, onTorchChange, onAiSceneDetectionChange, onExtensionModeChange, onTimelapseModeChange, onScanQrCodesChange, onCameraLensChange
             )
             
             // Row 2: Shutter and Gallery (and ModeSwitcher?)
             // User just said "toggles horizontal". Modeswitcher can stay near shutter.
             Box(
                 modifier = Modifier.fillMaxWidth(),
                 contentAlignment = Alignment.Center
             ) {
                 // Center: Mode + Shutter
                 Row(
                     verticalAlignment = Alignment.CenterVertically, 
                     horizontalArrangement = Arrangement.spacedBy(16.dp)
                 ) {
                      ModeSwitcher(isVerticalLayout = false, cameraMode, isProMode, onCameraModeChange, onProModeToggle)
                      ShutterButton(
                         cameraMode, isRecording, isPaused, timerSeconds, isTimerRunning, timelapseMode, timelapseFrames, recordingDurationNanos,
                         onCapture, onPauseRecording, onResumeRecording
                     )
                 }
                 
                 // Right: Gallery
                 Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)) {
                     GalleryButton(lastImageUri, onNavigateToGallery)
                 }
             }
        }
    }
}

@Composable
fun Toggles(
    isVerticalLayout: Boolean,
    flashModePref: Int,
    torchEnabled: Boolean,
    aiSceneDetection: Boolean,
    supportedExtensions: List<Int>,
    extensionMode: Int,
    cameraMode: Int,
    timelapseMode: Boolean,
    scanQrCodes: Boolean,
    lensFacing: Int,
    onFlashModeChange: (Int) -> Unit,
    onTorchChange: (Boolean) -> Unit,
    onAiSceneDetectionChange: (Boolean) -> Unit,
    onExtensionModeChange: (Int) -> Unit,
    onTimelapseModeChange: (Boolean) -> Unit,
    onScanQrCodesChange: (Boolean) -> Unit,
    onCameraLensChange: () -> Unit
) {
    if (isVerticalLayout) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ToggleButtonsContent(
                flashModePref, torchEnabled, aiSceneDetection, supportedExtensions, extensionMode, cameraMode, timelapseMode, scanQrCodes, lensFacing,
                onFlashModeChange, onTorchChange, onAiSceneDetectionChange, onExtensionModeChange, onTimelapseModeChange, onScanQrCodesChange, onCameraLensChange
            )
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        ) {
            ToggleButtonsContent(
                flashModePref, torchEnabled, aiSceneDetection, supportedExtensions, extensionMode, cameraMode, timelapseMode, scanQrCodes, lensFacing,
                onFlashModeChange, onTorchChange, onAiSceneDetectionChange, onExtensionModeChange, onTimelapseModeChange, onScanQrCodesChange, onCameraLensChange
            )
        }
    }
}

@Composable
fun ToggleButtonsContent(
    flashModePref: Int,
    torchEnabled: Boolean,
    aiSceneDetection: Boolean,
    supportedExtensions: List<Int>,
    extensionMode: Int,
    cameraMode: Int,
    timelapseMode: Boolean,
    scanQrCodes: Boolean,
    lensFacing: Int,
    onFlashModeChange: (Int) -> Unit,
    onTorchChange: (Boolean) -> Unit,
    onAiSceneDetectionChange: (Boolean) -> Unit,
    onExtensionModeChange: (Int) -> Unit,
    onTimelapseModeChange: (Boolean) -> Unit,
    onScanQrCodesChange: (Boolean) -> Unit,
    onCameraLensChange: () -> Unit
) {
    IconButton(onClick = { onFlashModeChange((flashModePref + 1) % 3) }) {
        Icon(when(flashModePref) { 1 -> Icons.Filled.FlashOn; 2 -> Icons.Filled.FlashOff; else -> Icons.Filled.FlashAuto }, "Flash", tint = Color.White)
    }
    IconButton(onClick = { onTorchChange(!torchEnabled) }) {
        Icon(if (torchEnabled) Icons.Filled.FlashlightOn else Icons.Filled.FlashlightOff, "Torch", tint = if (torchEnabled) Color.Yellow else Color.White)
    }
    IconButton(onClick = { onAiSceneDetectionChange(!aiSceneDetection) }) {
        Icon(Icons.Filled.Psychology, "AI", tint = if (aiSceneDetection) Color.Magenta else Color.White)
    }
    if (supportedExtensions.isNotEmpty() && cameraMode == 0) {
        IconButton(onClick = { 
            val idx = supportedExtensions.indexOf(extensionMode)
            val next = if (idx < supportedExtensions.size - 1) supportedExtensions[idx + 1] else ExtensionMode.NONE
            onExtensionModeChange(next)
        }) {
            Icon(Icons.Filled.AutoAwesome, "Ext", tint = if (extensionMode == ExtensionMode.NONE) Color.White else Color.Yellow)
        }
    }
    if (cameraMode == 1) {
        IconButton(onClick = { onTimelapseModeChange(!timelapseMode) }) {
            Icon(Icons.Filled.Timelapse, "Timelapse", tint = if (timelapseMode) Color.Red else Color.White)
        }
    }
    IconButton(onClick = { onScanQrCodesChange(!scanQrCodes) }) {
        Icon(Icons.Filled.QrCodeScanner, "QR", tint = if (scanQrCodes) Color.Cyan else Color.White)
    }
    IconButton(onClick = onCameraLensChange) {
        Icon(Icons.Filled.Cameraswitch, "Switch", tint = Color.White)
    }
}

@Composable
fun ProControls(
    isVerticalLayout: Boolean,
    isProMode: Boolean,
    exposureIndex: Int,
    exposureRange: android.util.Range<Int>,
    exposureStep: android.util.Rational,
    awbMode: Int,
    onExposureChange: (Int) -> Unit,
    onAwbModeChange: (Int) -> Unit
) {
    if (isProMode) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp)
                .then(if (isVerticalLayout) Modifier.fillMaxHeight() else Modifier.wrapContentHeight())
        ) {
            Text(
                "EV: ${if (exposureStep.numerator != 0) "%.1f".format(exposureIndex * exposureStep.numerator.toFloat() / exposureStep.denominator) else "0.0"}",
                color = Color.White, style = MaterialTheme.typography.labelSmall
            )
            
            if (isVerticalLayout) {
                // Vertical Layout (Sidebar)
                // Slider (Vertical)
                 Box(
                    modifier = Modifier
                        .weight(1f)
                        .width(64.dp), 
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
                             .width(160.dp)
                     )
                 }

                 // AWB (Vertical Column)
                 Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val modes = listOf(
                         CaptureRequest.CONTROL_AWB_MODE_AUTO to "A",
                         CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT to "Sun",
                         CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT to "Cld",
                         CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT to "Flu",
                         CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT to "Inc"
                     )
                     for ((mode, label) in modes) {
                         Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(if (awbMode == mode) Color.Yellow.copy(alpha=0.3f) else Color.Transparent).clickable { onAwbModeChange(mode) }.padding(horizontal = 4.dp, vertical = 2.dp)) {
                             Text(label, color = if (awbMode == mode) Color.Yellow else Color.White, style = MaterialTheme.typography.labelSmall)
                         }
                     }
                 }

            } else {
                // Horizontal Layout (Portrait)
                Slider(
                    value = exposureIndex.toFloat(),
                    onValueChange = { onExposureChange(it.roundToInt()) },
                    valueRange = exposureRange.lower.toFloat()..exposureRange.upper.toFloat(),
                    modifier = Modifier.width(120.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val modes = listOf(
                        CaptureRequest.CONTROL_AWB_MODE_AUTO to "Auto",
                        CaptureRequest.CONTROL_AWB_MODE_DAYLIGHT to "Sunny",
                        CaptureRequest.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT to "Cloud",
                        CaptureRequest.CONTROL_AWB_MODE_FLUORESCENT to "Fluo",
                        CaptureRequest.CONTROL_AWB_MODE_INCANDESCENT to "Inc"
                    )
                    for ((mode, label) in modes) {
                        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(if (awbMode == mode) Color.Yellow.copy(alpha=0.3f) else Color.Transparent).clickable { onAwbModeChange(mode) }.padding(horizontal = 4.dp, vertical = 2.dp)) {
                            Text(label, color = if (awbMode == mode) Color.Yellow else Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModeSwitcher(
    isVerticalLayout: Boolean,
    cameraMode: Int,
    isProMode: Boolean,
    onCameraModeChange: (Int) -> Unit,
    onProModeToggle: () -> Unit
) {
    val modifier = Modifier
        .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        .padding(4.dp) // Slightly tighter padding

    if (isVerticalLayout) {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CompactModeButton(Icons.Filled.PhotoCamera, cameraMode == 0) { onCameraModeChange(0) }
            CompactModeButton(Icons.Filled.Videocam, cameraMode == 1) { onCameraModeChange(1) }
            CompactModeButton(Icons.Filled.Settings, isProMode) { onProModeToggle() }
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactModeButton(Icons.Filled.PhotoCamera, cameraMode == 0) { onCameraModeChange(0) }
            CompactModeButton(Icons.Filled.Videocam, cameraMode == 1) { onCameraModeChange(1) }
            CompactModeButton(Icons.Filled.Settings, isProMode) { onProModeToggle() }
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
    onResumeRecording: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onCapture,
            modifier = Modifier.size(80.dp)
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
        modifier = Modifier.size(50.dp).border(2.dp, Color.White, CircleShape).background(Color.DarkGray, CircleShape)
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
            .padding(8.dp)
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(20.dp))
    }
}

// Helper
private fun formatDuration(nanos: Long): String {
    val seconds = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(nanos)
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}
