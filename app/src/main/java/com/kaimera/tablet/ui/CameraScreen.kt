package com.kaimera.tablet.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.DisposableEffect
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.view.PreviewView
import android.util.Size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import kotlin.math.roundToInt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.kaimera.tablet.data.UserPreferencesRepository
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.camera.core.FocusMeteringAction
import androidx.camera.video.MediaStoreOutputOptions
import androidx.core.util.Consumer
import androidx.camera.video.VideoRecordEvent
import androidx.camera.extensions.ExtensionMode
import com.kaimera.tablet.camera.CameraManager

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(onNavigateToGallery: () -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferencesRepository(context) }

    val gridRows by userPreferences.gridRows.collectAsState(initial = 2)
    val gridCols by userPreferences.gridCols.collectAsState(initial = 2)
    val timerSeconds by userPreferences.timerSeconds.collectAsState(initial = 0)
    val flashModePref by userPreferences.flashMode.collectAsState(initial = 2)
    val photoResolutionTier by userPreferences.photoResolutionTier.collectAsState(initial = 1)
    val videoResolutionTier by userPreferences.videoResolutionTier.collectAsState(initial = 1)
    val videoFps by userPreferences.videoFps.collectAsState(initial = 30)
    val jpegQuality by userPreferences.jpegQuality.collectAsState(initial = 95)
    val circleRadiusPercent by userPreferences.circleRadiusPercent.collectAsState(initial = 20)
    val captureMode by userPreferences.captureMode.collectAsState(initial = 1)
    val isDebugMode by userPreferences.isDebugMode.collectAsState(initial = false)
    val scanQrCodes by userPreferences.scanQrCodes.collectAsState(initial = false)

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    if (permissionsState.allPermissionsGranted) {
        val cameraManager = remember { CameraManager(context) }
        CameraContent(
            context = context,
            lifecycleOwner = lifecycleOwner,
            onNavigateToGallery = onNavigateToGallery,
            cameraManager = cameraManager,
            gridRows = gridRows,
            gridCols = gridCols,
            timerSeconds = timerSeconds,
            flashModePref = flashModePref,
            photoResolutionTier = photoResolutionTier,
            videoResolutionTier = videoResolutionTier,
            videoFps = videoFps,
            jpegQuality = jpegQuality,
            circleRadiusPercent = circleRadiusPercent,
            captureMode = captureMode,
            isDebugMode = isDebugMode,
            scanQrCodes = scanQrCodes,
            onFlashModeChange = { newMode -> 
                scope.launch { 
                    userPreferences.setFlashMode(newMode)
                    cameraManager.setFlashMode(newMode)
                }
            },
            onScanQrCodesChange = { enabled ->
                scope.launch {
                    userPreferences.setScanQrCodes(enabled)
                }
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Camera permissions required")
                Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                    Text("Grant Permissions")
                }
            }
        }
    }
}

@Composable
fun CameraContent(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    onNavigateToGallery: () -> Unit,
    cameraManager: CameraManager,
    gridRows: Int,
    gridCols: Int,
    timerSeconds: Int,
    flashModePref: Int,
    photoResolutionTier: Int,
    videoResolutionTier: Int,
    videoFps: Int,
    jpegQuality: Int,
    circleRadiusPercent: Int,
    captureMode: Int,
    isDebugMode: Boolean,
    scanQrCodes: Boolean,
    onFlashModeChange: (Int) -> Unit,
    onScanQrCodesChange: (Boolean) -> Unit
) {
    // Camera Manager State
    val zoomRatio by cameraManager.zoomState.collectAsState()
    val maxZoomRatio by cameraManager.maxZoomState.collectAsState()
    val isRecording by cameraManager.isRecording.collectAsState()
    val isPaused by cameraManager.isPaused.collectAsState()
    val detectedQrCode by cameraManager.detectedQrCode.collectAsState()
    
    // View State (for dynamic rebinding)
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var cameraMode by remember { mutableStateOf(0) } // 0: Photo, 1: Video

    // Timer State
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerCountdown by remember { mutableStateOf(0) }
    
    // Recording Duration
    val recordingDurationNanos by cameraManager.recordingDurationNanos.collectAsState()

    // Focus State
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    
    // Level Sensor State
    var isLevel by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

    // Pro Mode State
    var isProMode by remember { mutableStateOf(false) }
    val exposureIndex by cameraManager.exposureIndex.collectAsState()
    val exposureRange by cameraManager.exposureRange.collectAsState()
    val exposureStep by cameraManager.exposureStep.collectAsState()

    val actualResolution by cameraManager.actualResolution.collectAsState()

    // Extensions State
    val supportedExtensions by cameraManager.supportedExtensions.collectAsState()
    var extensionMode by remember { mutableIntStateOf(ExtensionMode.NONE) }
    
    // Reset extension mode if it's no longer supported (e.g. lens switch)
    LaunchedEffect(supportedExtensions) {
        if (extensionMode != ExtensionMode.NONE && !supportedExtensions.contains(extensionMode)) {
            extensionMode = ExtensionMode.NONE
        }
    }


    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    val x = it.values[0]
                    val y = it.values[1]
                    
                    // Calculate rotation angle (roll)
                    // atan2(x, y) gives angle in radians relative to Y axis
                    // If device is portrait upright: x~0, y~9.8 -> angle 0
                    // If device is landscape (left): x~9.8, y~0 -> angle 90
                    // We want to rotate the crosshairs OPPOSITE to device tilt to keep them horizontal.
                    val angleRad = kotlin.math.atan2(x.toDouble(), y.toDouble())
                    val angleDeg = Math.toDegrees(angleRad).toFloat()
                    rotationAngle = angleDeg

                    // Level logic: Check if aligned to 0, 90, 180, 270 within threshold
                    val normalizedAngle = (Math.abs(angleDeg) % 90)
                    val threshold = 1.0 // Degrees
                    // Check deviation from nearest quadrant
                    val deviation = if (normalizedAngle > 45) 90 - normalizedAngle else normalizedAngle
                    
                    isLevel = deviation < threshold
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    // Thumbnail State
    var lastImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    
    LaunchedEffect(Unit) {
        lastImageUri = getLastImageUri(context)
    }

    // Timer Logic for Shutter Delay
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (timerCountdown > 0) {
                delay(1000)
                timerCountdown--
            }
            isTimerRunning = false
            if (cameraMode == 0) {
                cameraManager.takePhoto { uri ->
                    lastImageUri = uri
                }
            } else {
                cameraManager.startVideoRecording { uri ->
                    lastImageUri = uri
                }
            }
        }
    }

    // Camera Re-binding logic
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val maxWidthDp = maxWidth
        val maxHeightDp = maxHeight
        val isPortraitWindow = maxHeight > maxWidth

        LaunchedEffect(lensFacing, cameraMode, previewView, photoResolutionTier, videoResolutionTier, videoFps, jpegQuality, captureMode, extensionMode, scanQrCodes, maxWidthDp, maxHeightDp) {
            val view = previewView ?: return@LaunchedEffect
            val windowSize = android.util.Size(maxWidthDp.value.toInt(), maxHeightDp.value.toInt())
            if (cameraMode == 0) {
                cameraManager.bindPhotoPreview(
                    lifecycleOwner, 
                    view, 
                    lensFacing, 
                    flashMode = flashModePref,
                    photoResolutionTier = photoResolutionTier,
                    jpegQuality = jpegQuality,
                    captureMode = captureMode,
                    extensionMode = extensionMode,
                    windowSize = windowSize,
                    scanQrCodes = scanQrCodes
                )
            } else {
                cameraManager.bindVideoPreview(
                    lifecycleOwner, 
                    view, 
                    lensFacing, 
                    videoResolutionTier = videoResolutionTier,
                    targetFps = videoFps,
                    windowSize = windowSize,
                    scanQrCodes = scanQrCodes
                )
            }
        }


    // Note: BoxWithConstraints replaces the outer Box
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                PreviewView(ctx).also { view ->
                    previewView = view
                    view.setOnTouchListener { v, event ->
                        if (event.action == android.view.MotionEvent.ACTION_UP) {
                            cameraManager.focus(view, event.x, event.y)
                            v.performClick()
                        }
                        true
                    }
                }
            },
            update = { }
        )

        // Camera Overlays (Grid, Level, Focus)
        CameraOverlays(
            gridRows = gridRows,
            gridCols = gridCols,
            circleRadiusPercent = circleRadiusPercent,
            isLevel = isLevel,
            rotationAngle = rotationAngle,
            focusPoint = focusPoint
        )

        // Detected QR Code Result Pill
        detectedQrCode?.let { qrText ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, Color.Cyan.copy(alpha = 0.5f)),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Link, contentDescription = null, tint = Color.Cyan, modifier = Modifier.size(20.dp))
                        Text(
                            text = qrText,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 200.dp)
                        )
                        IconButton(
                            onClick = { cameraManager.clearDetectedQrCode() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // Overlay Controls - Updated Polished Layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding() // Add margin for system bars
                .padding(vertical = 16.dp), // Extra margin for aesthetics
            horizontalArrangement = Arrangement.End
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // Sidebar Surface
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Vertical Zoom Slider Column
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Filled.ZoomIn, "Zoom In", tint = Color.White, modifier = Modifier.size(20.dp))
                    
                    // Vertical Slider using Layout to rotate constraints properly
                    // Simple rotation approach
                    Box(
                        modifier = Modifier
                            .height(200.dp)
                            .width(60.dp), // Increased width for better touch target/thumb
                        contentAlignment = Alignment.Center
                    ) {
                        Slider(
                            value = zoomRatio,
                            onValueChange = { 
                                cameraManager.setZoom(it)
                            },
                            valueRange = 1f..maxZoomRatio,
                            modifier = Modifier
                                .rotate(-90f)
                                .requiredWidth(200.dp) // Use requiredWidth to bypass 60dp constraint
                        )
                    }

                    Icon(Icons.Filled.ZoomOut, "Zoom Out", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                // 2. Main Controls Column
                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween, // Distribute Top/Center/Bottom
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TOP: Flash, Flip
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                         IconButton(onClick = { 
                            val newMode = (flashModePref + 1) % 3 
                            onFlashModeChange(newMode)
                        }) {
                            Icon(
                                imageVector = when(flashModePref) {
                                    1 -> Icons.Filled.FlashOn
                                    2 -> Icons.Filled.FlashOff
                                    else -> Icons.Filled.FlashAuto
                                },
                                contentDescription = "Flash",
                                tint = Color.White
                            )
                        }
                        


                        // Extension Toggle (Only if extensions are available)
                        if (supportedExtensions.isNotEmpty() && cameraMode == 0) {
                             IconButton(onClick = { 
                                // Cycle: None -> Ext 1 -> Ext 2 -> ... -> None
                                val currentIndex = supportedExtensions.indexOf(extensionMode)
                                extensionMode = if (currentIndex < supportedExtensions.size - 1) {
                                    supportedExtensions[currentIndex + 1]
                                } else {
                                    ExtensionMode.NONE
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome, 
                                    contentDescription = "Extensions", 
                                    tint = if (extensionMode == ExtensionMode.NONE) Color.White else Color.Yellow
                                )
                            }
                        }

                        // QR Code Scanner Toggle
                         IconButton(onClick = { 
                            onScanQrCodesChange(!scanQrCodes)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.QrCodeScanner, 
                                contentDescription = "QR Scanner", 
                                tint = if (scanQrCodes) Color.Cyan else Color.White
                            )
                        }
                        
                         IconButton(onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                        }) {
                            Icon(Icons.Filled.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White)
                        }
                    }

                    // CENTER: Shutter, Mode, & Pro Controls
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        
                        // Pro Control Panel (Visible only in Pro Mode)
                        if (isProMode) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "EV: ${
                                        if (exposureStep.numerator != 0) 
                                            "%.1f".format(exposureIndex * exposureStep.numerator.toFloat() / exposureStep.denominator) 
                                        else "0.0"
                                    }",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Slider(
                                    value = exposureIndex.toFloat(),
                                    onValueChange = { cameraManager.setExposureCompensationIndex(it.roundToInt()) },
                                    valueRange = exposureRange.lower.toFloat()..exposureRange.upper.toFloat(),
                                    modifier = Modifier.width(120.dp)
                                )
                            }
                        }

                        // Compact Mode Switch & Pro Toggle
                        Row(
                            modifier = Modifier
                                .background(Color.DarkGray.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CompactModeButton(
                                icon = Icons.Filled.PhotoCamera,
                                isSelected = cameraMode == 0,
                                onClick = { cameraMode = 0 }
                            )
                            CompactModeButton(
                                icon = Icons.Filled.Videocam,
                                isSelected = cameraMode == 1,
                                onClick = { cameraMode = 1 }
                            )
                             CompactModeButton(
                                icon = Icons.Filled.Settings, // Using Settings icon as fallback for Tune
                                isSelected = isProMode,
                                onClick = { isProMode = !isProMode }
                            )
                        }

                        // Shutter Button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             IconButton(
                                onClick = {
                                    if (cameraMode == 1) {
                                        if (isRecording) {
                                            cameraManager.stopVideoRecording()
                                        } else {
                                            if (timerSeconds > 0) {
                                                isTimerRunning = true
                                                timerCountdown = timerSeconds
                                            } else {
                                                cameraManager.startVideoRecording { uri ->
                                                    lastImageUri = uri
                                                }
                                            }
                                        }
                                    } else {
                                        if (timerSeconds > 0) {
                                            isTimerRunning = true
                                            timerCountdown = timerSeconds
                                        } else {
                                            cameraManager.takePhoto { uri ->
                                                 lastImageUri = uri
                                            }
                                        }
                                    }
                                },
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
                            
                            // Recording Duration & Pause Control
                            if (cameraMode == 1 && isRecording) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically, 
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            if (isPaused) cameraManager.resumeVideoRecording()
                                            else cameraManager.pauseVideoRecording()
                                        },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(Color.White.copy(alpha=0.3f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = if (isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                                            contentDescription = if (isPaused) "Resume" else "Pause",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    
                                    Text(
                                        text = formatDuration(recordingDurationNanos),
                                        color = if (isPaused) Color.Yellow else Color.Red,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                    }

                    // BOTTOM: Gallery (Aligned with Shutter somewhat or just at bottom)
                    // Pushed to bottom by SpaceBetween
                    IconButton(
                        onClick = onNavigateToGallery,
                        modifier = Modifier
                            .size(50.dp)
                            .border(2.dp, Color.White, CircleShape)
                            .background(Color.DarkGray, CircleShape)
                    ) {
                        if (lastImageUri != null) {
                            coil.compose.AsyncImage(
                                model = lastImageUri,
                                contentDescription = "Gallery",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }
            
        // Timer Countdown Display (Centered)
        if (isTimerRunning) {
             Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
             ) {
                 Text(
                    text = "$timerCountdown",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontSize = 120.sp
                 )
             }
        }

        // Debug Overlay
        if (isDebugMode) {
             Box(
                 modifier = Modifier
                     .fillMaxWidth()
                     .padding(top = 32.dp)
                     .background(Color.Red.copy(alpha = 0.5f)),
                 contentAlignment = Alignment.Center
             ) {
                 Column(horizontalAlignment = Alignment.CenterHorizontally) {
                     Text("Debug Mode", color = Color.White, style = MaterialTheme.typography.labelSmall)
                     Text("Window: $maxWidthDp x $maxHeightDp", color = Color.White)
                     Text("Actual: ${actualResolution.width}x${actualResolution.height}", color = Color.White)
                     val ratio = if (actualResolution.height != 0) actualResolution.width.toFloat() / actualResolution.height.toFloat() else 0f
                     Text("Ratio: %.2f".format(ratio), color = Color.White)
                     Text("isPortrait: $isPortraitWindow", color = Color.White)

                     Text("Lens: $lensFacing", color = Color.White)
                     Text("Ext: $extensionMode (Sup: ${supportedExtensions.size})", color = Color.White)
                 }
             }
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
    }
}


// Legacy functions removed.

private fun formatDuration(nanos: Long): String {
    val seconds = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(nanos)
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

private fun getLastImageUri(context: Context): android.net.Uri? {
    val projection = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATE_ADDED
    )
    val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
    val cursor = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
    )
    return cursor?.use {
        if (it.moveToFirst()) {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val id = it.getLong(idColumn)
            android.content.ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        } else {
            null
        }
    }
}
