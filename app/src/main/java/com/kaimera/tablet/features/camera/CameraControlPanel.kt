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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.zIndex
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
    // Quad-Border Cockpit Layout
    BoxWithConstraints(
        modifier = Modifier.fillMaxSize()
    ) {
        val width = maxWidth
        val height = maxHeight
        // Simple logic for now: 
        // Landscape (Tablet) -> Show All
        // Portrait -> Show Top/Bottom, Hide Sides (or merge)
        // Small Window -> Only Bottom

        val isLandscape = width > height
        val isWide = width > 600.dp
        
        // Priority Logic (Disabled hiding for now as per user request)
        val showSideBars = true // Changed from isLandscape || isWide
        val showTopBar = height > 300.dp // Slightly relaxed top bar hiding

        // 1. Bottom Bar (Always Visible - Z-Index High)
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .zIndex(10f)
        ) {
            CockpitBottomBar(
                cameraMode, isRecording, isPaused, timerSeconds, isTimerRunning, timelapseMode, timelapseFrames, recordingDurationNanos,
                lastImageUri, lensFacing, isProMode,
                onCapture, onPauseRecording, onResumeRecording, onNavigateToGallery, onCameraModeChange, onCameraLensChange, onProModeToggle
            )
        }

        // 2. Top Bar (Priority 4)
        if (showTopBar) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .zIndex(9f)
            ) {
                CockpitTopBar(
                    flashModePref, torchEnabled, aiSceneDetection, supportedExtensions, extensionMode, cameraMode, timelapseMode, scanQrCodes,
                    onFlashModeChange, onTorchChange, onAiSceneDetectionChange, onExtensionModeChange, onTimelapseModeChange, onScanQrCodesChange
                )
            }
        }

        // 3. Right Sidebar (Zoom - Priority 2)
        if (showSideBars) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .padding(vertical = if(showTopBar) 48.dp else 0.dp) // Reduced from 56
                    .padding(bottom = 84.dp) // Reduced from 100
                    .zIndex(8f)
            ) {
                CockpitSideBarRight(zoomRatio, maxZoomRatio, onZoomChange)
            }
        
            // 4. Left Sidebar (Pro - Priority 3)
            if (isProMode) {
                 Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxHeight()
                        .padding(vertical = if(showTopBar) 48.dp else 0.dp) // Reduced from 56
                        .padding(bottom = 84.dp) // Reduced from 100
                        .zIndex(8f)
                ) {
                    CockpitSideBarLeft(exposureIndex, exposureRange, exposureStep, awbMode, onExposureChange, onAwbModeChange)
                }
            }
        }
    }
}
