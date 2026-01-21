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
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.AspectRatio
import android.util.Size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
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
    val flashModePref by userPreferences.flashMode.collectAsState(initial = 0)
    val resolutionTier by userPreferences.resolutionTier.collectAsState(initial = 0)
    val jpegQuality by userPreferences.jpegQuality.collectAsState(initial = 95)
    val circleRadiusPercent by userPreferences.circleRadiusPercent.collectAsState(initial = 20)

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
        CameraContent(
            context = context,
            lifecycleOwner = lifecycleOwner,
            onNavigateToGallery = onNavigateToGallery,
            gridRows = gridRows,
            gridCols = gridCols,
            timerSeconds = timerSeconds,
            flashModePref = flashModePref,
            resolutionTier = resolutionTier,
            jpegQuality = jpegQuality,
            circleRadiusPercent = circleRadiusPercent,
            onFlashModeChange = { newMode -> 
                scope.launch { userPreferences.setFlashMode(newMode) }
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
    gridRows: Int,
    gridCols: Int,
    timerSeconds: Int,
    flashModePref: Int,
    resolutionTier: Int,
    jpegQuality: Int,
    circleRadiusPercent: Int,
    onFlashModeChange: (Int) -> Unit
) {
    // CameraProvider State
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    
    // View State (for dynamic rebinding)
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoomRatio by remember { mutableFloatStateOf(1f) }
    var cameraMode by remember { mutableStateOf(0) } // 0: Photo, 1: Video
    var isRecording by remember { mutableStateOf(false) }

    // Timer State
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerCountdown by remember { mutableStateOf(0) }
    
    // Recording Duration
    var recordingDurationNanos by remember { mutableLongStateOf(0L) }

    // Focus State
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    
    // Level Sensor State
    var isLevel by remember { mutableStateOf(false) }
    var rotationAngle by remember { mutableFloatStateOf(0f) }

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

    // Flash State (Camera Control)
    var currentCameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    
    // Update Camera Flash Mode based on Preference
    LaunchedEffect(currentCameraControl, flashModePref, cameraMode) {
        currentCameraControl?.let { control ->
            if (cameraMode == 0) { // Photo Mode
                imageCapture?.flashMode = when(flashModePref) {
                    1 -> ImageCapture.FLASH_MODE_ON
                    2 -> ImageCapture.FLASH_MODE_AUTO
                    else -> ImageCapture.FLASH_MODE_OFF
                }
                control.enableTorch(false) 
            } else { // Video Mode
                 control.enableTorch(flashModePref == 1)
            }
        }
    }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
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
                takePhoto(context, imageCapture) { uri ->
                    lastImageUri = uri
                }
            } else {
                isRecording = true // Trigger actual recording start
            }
        }
    }
    
    // Video Recording Logic
    LaunchedEffect(isRecording) {
        if (isRecording) {
            val videoCap = videoCapture ?: return@LaunchedEffect
            val newRecording = startRecording(context, videoCap, cameraExecutor) { event ->
                if (event is VideoRecordEvent.Status) {
                   recordingDurationNanos = event.recordingStats.recordedDurationNanos
                } else if (event is VideoRecordEvent.Finalize) {
                   if (!event.hasError()) {
                       val uri = event.outputResults.outputUri
                       if (uri != android.net.Uri.EMPTY) {
                           lastImageUri = uri
                       }
                       val msg = "Video capture succeeded: ${event.outputResults.outputUri}"
                       Log.d("CameraScreen", msg)
                   } else {
                       recording?.close()
                       recording = null
                       Log.e("CameraScreen", "Video capture failed: ${event.error}")
                   }
                }
            }
            recording = newRecording
        } else {
            recording?.stop()
            recording = null
            recordingDurationNanos = 0L
        }
    }

    // Dynamic Camera Re-binding logic
    // Dynamic Camera Re-binding logic
    LaunchedEffect(cameraProvider, lensFacing, cameraMode, resolutionTier, jpegQuality, previewView) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val view = previewView ?: return@LaunchedEffect
        
        try {
            provider.unbindAll()
            
            // 1. Resolution Logic
            val resolutionStrategy = when(resolutionTier) {
                0 -> ResolutionStrategy(Size(1280, 720), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
                2 -> ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY 
                else -> ResolutionStrategy(Size(1920, 1080), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER) 
            }
            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(resolutionStrategy)
                .build()

            // 2. Preview UseCase
            val prev = Preview.Builder()
                .setResolutionSelector(resolutionSelector)
                .build()
            
            prev.setSurfaceProvider(view.surfaceProvider)
            preview = prev // Update state

            // 3. ImageCapture UseCase
            val imgCap = ImageCapture.Builder()
                .setResolutionSelector(resolutionSelector)
                .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                .build()
            imageCapture = imgCap

            // 4. VideoCapture UseCase
            val quality = when(resolutionTier) {
                0 -> Quality.HD
                2 -> Quality.UHD
                else -> Quality.FHD
            }
            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(quality))
                .build()
            val vidCap = VideoCapture.withOutput(recorder)
            videoCapture = vidCap

            // 5. Bind
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            
            val camera = if (cameraMode == 0) {
                 provider.bindToLifecycle(lifecycleOwner, cameraSelector, prev, imgCap)
            } else {
                 provider.bindToLifecycle(lifecycleOwner, cameraSelector, prev, vidCap)
            }
            
            currentCameraControl = camera.cameraControl
            
            camera.cameraInfo.zoomState.observe(lifecycleOwner) { state ->
                zoomRatio = state.zoomRatio
                maxZoomRatio = state.maxZoomRatio
            }
            
        } catch (exc: Exception) {
            Log.e("CameraScreen", "Camera binding failed", exc)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                val view = PreviewView(ctx)
                previewView = view // Assign state
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider
                    
                    // UseCases and Binding are handled by LaunchedEffect
                    
                    view.setOnTouchListener { v, event ->
                            if (event.action == android.view.MotionEvent.ACTION_UP) {
                                val factory = view.meteringPointFactory
                                val point = factory.createPoint(event.x, event.y)
                                val action = FocusMeteringAction.Builder(point).build()
                                currentCameraControl?.startFocusAndMetering(action)
                                v.performClick()
                            }
                            true
                        }

                }, ContextCompat.getMainExecutor(ctx))

                view
            },
            update = { }
        )

        // Focus Indicator
        focusPoint?.let { offset ->
            Box(modifier = Modifier.fillMaxSize()) {
                 Canvas(modifier = Modifier.fillMaxSize()) {
                      drawCircle(Color.Yellow, radius = 30f, center = offset, style = Stroke(width = 3f))
                 }
            }
        }
        
        // Grid Overlay
        if (gridRows > 0 || gridCols > 0) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                
                if (gridRows > 0) {
                    val rowHeight = height / (gridRows + 1)
                    for (i in 1..gridRows) {
                        val y = rowHeight * i
                        drawLine(
                            color = Color.White.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 2f
                        )
                    }
                }
                
                if (gridCols > 0) {
                    val colWidth = width / (gridCols + 1)
                    for (i in 1..gridCols) {
                        val x = colWidth * i
                         drawLine(
                            color = Color.White.copy(alpha = 0.5f),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 2f
                        )
                    }
                }
            }
        }
        
        // Center Circle Overlay
        if (circleRadiusPercent > 0) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val centerX = width / 2
                val centerY = height / 2
                val maxDimension = maxOf(width, height)
                val radius = (maxDimension * (circleRadiusPercent / 100f)) / 2f
                
                val overlayColor = if (isLevel) Color.Green else Color.White.copy(alpha = 0.5f)
                val strokeStyle = Stroke(width = 3f)

                // Draw Circle (Does not rotate with crosshairs to keep it screen-aligned, or should it?)
                // User said "crosshaiirs should rotate".
                // Usually circle stays fixed.
                drawCircle(
                    color = overlayColor,
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = strokeStyle
                )
                
                // Draw Crosshairs (Rotated)
                rotate(degrees = rotationAngle, pivot = Offset(centerX, centerY)) {
                     // Draw long lines to Ensure cover screen when rotated
                     val longDimension = maxDimension * 2f
                     
                     // Horizontal
                     // Left
                     drawLine(
                        color = overlayColor,
                        start = Offset(centerX - radius, centerY),
                        end = Offset(centerX - longDimension, centerY),
                        strokeWidth = 2f
                    )
                     // Right
                     drawLine(
                        color = overlayColor,
                        start = Offset(centerX + radius, centerY),
                        end = Offset(centerX + longDimension, centerY),
                        strokeWidth = 2f
                    )
                    
                    // Vertical
                    // Top
                    drawLine(
                        color = overlayColor,
                        start = Offset(centerX, centerY - radius),
                        end = Offset(centerX, centerY - longDimension),
                        strokeWidth = 2f
                    )
                    // Bottom
                    drawLine(
                        color = overlayColor,
                        start = Offset(centerX, centerY + radius),
                        end = Offset(centerX, centerY + longDimension),
                        strokeWidth = 2f
                    )
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
                                zoomRatio = it
                                currentCameraControl?.setZoomRatio(it)
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
                                    2 -> Icons.Filled.FlashAuto
                                    else -> Icons.Filled.FlashOff
                                },
                                contentDescription = "Flash",
                                tint = Color.White
                            )
                        }
                        
                         IconButton(onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                        }) {
                            Icon(Icons.Filled.Cameraswitch, contentDescription = "Switch Camera", tint = Color.White)
                        }
                    }

                    // CENTER: Shutter & Mode
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Compact Mode Switch
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
                        }

                        // Shutter Button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                             IconButton(
                                onClick = {
                                    if (cameraMode == 1) {
                                        if (isRecording) {
                                            isRecording = false // This triggers the LaunchedEffect to stop
                                        } else {
                                            if (timerSeconds > 0) {
                                                isTimerRunning = true
                                                timerCountdown = timerSeconds
                                            } else {
                                                isRecording = true // This triggers the LaunchedEffect to start
                                            }
                                        }
                                    } else {
                                        if (timerSeconds > 0) {
                                            isTimerRunning = true
                                            timerCountdown = timerSeconds
                                        } else {
                                            takePhoto(context, imageCapture) { uri ->
                                                 lastImageUri = uri
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.size(80.dp)
                            ) {
                                Icon(
                                    imageVector = if (cameraMode == 1 && isRecording) Icons.Filled.Stop else Icons.Filled.PhotoCamera,
                                    contentDescription = "Capture",
                                    tint = if (cameraMode == 1) Color.Red else Color.White,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            
                            // Recording Duration Label
                            if (cameraMode == 1 && isRecording) {
                                Text(
                                    text = formatDuration(recordingDurationNanos),
                                    color = Color.Red,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // BOTTOM: Gallery (Aligned with Shutter somewhat or just at bottom)
                    // Pushed to bottom by SpaceBetween
                    IconButton(
                        onClick = onNavigateToGallery,
                        modifier = Modifier
                            .size(50.dp)
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

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture?,
    onPhotoTaken: (android.net.Uri) -> Unit = {}
) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Kaimera")
        }
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture?.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("CameraScreen", "Photo capture failed: ${exc.message}", exc)
                Toast.makeText(context, "Photo capture failed", Toast.LENGTH_SHORT).show()
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val msg = "Photo capture succeeded"
                // Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                Log.d("CameraScreen", msg)
                output.savedUri?.let { onPhotoTaken(it) }
            }
        }
    )
}

private fun startRecording(
    context: Context,
    videoCapture: VideoCapture<Recorder>,
    executor: ExecutorService,
    eventListener: Consumer<VideoRecordEvent>
): Recording {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
        
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Kaimera")
        }
    }

    val mediaStoreOutputOptions = MediaStoreOutputOptions.Builder(
        context.contentResolver,
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    ).setContentValues(contentValues)
    .build()

    return videoCapture.output
        .prepareRecording(context, mediaStoreOutputOptions)
        .apply {
            if (androidx.core.content.PermissionChecker.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                androidx.core.content.PermissionChecker.PERMISSION_GRANTED)
            {
                withAudioEnabled()
            }
        }
        .start(executor, eventListener) // USE EXECUTOR HERE
}

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
