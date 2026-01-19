package com.kaimera.tablet.ui

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
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
import androidx.camera.core.CameraEffect
import androidx.camera.core.UseCaseGroup
import com.kaimera.tablet.rendering.FilterSurfaceProcessor
import com.kaimera.tablet.rendering.TextureRenderer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.platform.LocalConfiguration
import android.view.OrientationEventListener
import android.view.Surface


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(onNavigateToGallery: () -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val userPreferences = remember { UserPreferencesRepository(context) }

    val gridRows by userPreferences.gridRows.collectAsState(initial = 0)
    val gridCols by userPreferences.gridCols.collectAsState(initial = 0)
    val timerSeconds by userPreferences.timerSeconds.collectAsState(initial = 0)
    val flashModePref by userPreferences.flashMode.collectAsState(initial = 0)

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
    onFlashModeChange: (Int) -> Unit
) {
    // CameraProvider State
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    
    var preview by remember { mutableStateOf<Preview?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoomRatio by remember { mutableFloatStateOf(1f) }
    var cameraMode by remember { mutableStateOf(0) } // 0: Photo, 1: Video
    var isRecording by remember { mutableStateOf(false) }

    // Orientation State for UI Rotation
    var rotationDegrees by remember { mutableStateOf(0) }
    val rotationAnimation by animateFloatAsState(
        targetValue = -rotationDegrees.toFloat(),
        animationSpec = tween(durationMillis = 300), 
        label = "rotation"
    )


    
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val orientationEventListener = object : OrientationEventListener(context) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) return
                // Check if device is flat? No, standard logic.
                
                // Determine the rotation of the screen relative to the device's natural orientation.
                // Since we locked to Landscape, the Activity considers "0" to be Landscape.
                // But we want the icons to rotate relative to Gravity.
                
                // If I hold tablet in Landscape (Home button right), orientation is 0 (or 90? Tablet natural is usually varied).
                // Let's assume standard behavior:
                // 0 = Normal
                // 90 = Right Side Down
                // 180 = Upside Down
                // 270 = Left Side Down
                
                val newRotation = when (orientation) {
                    in 315..360, in 0..45 -> 90
                    in 46..135 -> 180 
                    in 136..225 -> 270
                    in 226..315 -> 0
                    else -> 90
                }
                
                // For UI rotation, we want to counter-rotate.
                // If device rotates 90deg clockwise, we want UI to rotate 90deg counter-clockwise relative to screen (which is now physical 90).
                // Wait. We LOCKED the screen to Landscape.
                // So the Screen (Activity) is ALWAYS at Surface.ROTATION_0 or ROTATION_90 depending on device natural.
                // If it's "userLandscape", it will flip between 0 and 180 (if supported) or stay fixed.
                // Let's assume the Activity does NOT rotate.
                // So we just match gravity.
                
                if (newRotation != rotationDegrees) {
                    rotationDegrees = newRotation
                }
            }
        }
        orientationEventListener.enable()
        onDispose {
            orientationEventListener.disable()
        }
    }

    // Timer State
    var isTimerRunning by remember { mutableStateOf(false) }
    var timerCountdown by remember { mutableStateOf(0) }
    
    // Recording Duration
    var recordingDurationNanos by remember { mutableLongStateOf(0L) }

    // Focus State
    var focusPoint by remember { mutableStateOf<Offset?>(null) }

    // Filter State
    var selectedFilter by remember { mutableStateOf(TextureRenderer.FilterType.NORMAL) }
    var filterProcessor by remember { mutableStateOf<FilterSurfaceProcessor?>(null) }
    var showFilterMenu by remember { mutableStateOf(false) }

    // Update Filter
    LaunchedEffect(selectedFilter) {
        filterProcessor?.setFilter(selectedFilter)
    }

    // Cleanup on dispose
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose {
            filterProcessor?.release()
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
    LaunchedEffect(cameraProvider, lensFacing, cameraMode) {
        val provider = cameraProvider ?: return@LaunchedEffect
        val prev = preview ?: return@LaunchedEffect
        
        Log.d("CameraScreen", "Binding camera: mode=$cameraMode, lens=$lensFacing")
        try {
            provider.unbindAll()
            Log.d("CameraScreen", "Unbound all use cases")
            
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            
            // Clean up previous processor if exists
            filterProcessor?.release()
            val proc = FilterSurfaceProcessor()
            filterProcessor = proc
            Log.d("CameraScreen", "Created new FilterSurfaceProcessor: $proc")

            // Asymmetric Targeting:
            // Unify targets to include ALL potential use cases to prevent pipeline thrashing on mode switch.
            // Even if a use case isn't bound (like ImageCapture in video mode), it's safe to target it.
            val targets = CameraEffect.PREVIEW or CameraEffect.VIDEO_CAPTURE or CameraEffect.IMAGE_CAPTURE
            Log.d("CameraScreen", "CameraEffect targets: $targets")

            // CameraEffect is abstract/protected constructor, so we need a subclass
            val cameraEffect = object : CameraEffect(
                targets,
                cameraExecutor,
                proc,
                { error -> Log.e("CameraScreen", "CameraEffect error: ${error.cause}", error.cause) }
            ) {}
            
            val useCaseGroupBuilder = UseCaseGroup.Builder()
            // Bind ALL use cases regardless of mode to satisfy CameraEffect targets (P+V+I).
            // This prevents StreamSharing from failing due to missing children.
            val imgCap = imageCapture ?: return@LaunchedEffect
            val vidCap = videoCapture ?: return@LaunchedEffect
            
            useCaseGroupBuilder.addUseCase(prev)
            useCaseGroupBuilder.addUseCase(imgCap)
            useCaseGroupBuilder.addUseCase(vidCap)
            useCaseGroupBuilder.addEffect(cameraEffect)
            
            val useCaseGroup = useCaseGroupBuilder.build()
            Log.d("CameraScreen", "Binding to lifecycle...")
            val camera = provider.bindToLifecycle(lifecycleOwner, cameraSelector, useCaseGroup)
            Log.d("CameraScreen", "Bind complete.")
            
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
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    val provider = cameraProviderFuture.get()
                    cameraProvider = provider
                    
                    Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                        preview = it
                    }

                    val imageCaptureUseCase = ImageCapture.Builder()
                        .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
                        .build()
                    imageCapture = imageCaptureUseCase

                    val recorder = Recorder.Builder()
                        .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                        .build()
                    val videoCaptureUseCase = VideoCapture.withOutput(recorder)
                    videoCapture = videoCaptureUseCase

                    // val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                    
                    previewView.setOnTouchListener { view, event ->
                            if (event.action == android.view.MotionEvent.ACTION_UP) {
                                val factory = previewView.meteringPointFactory
                                val point = factory.createPoint(event.x, event.y)
                                val action = FocusMeteringAction.Builder(point).build()
                                currentCameraControl?.startFocusAndMetering(action)
                                view.performClick()
                            }
                            true
                        }

                }, ContextCompat.getMainExecutor(ctx))

                previewView
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
                    Icon(
                        Icons.Filled.ZoomIn, 
                        "Zoom In", 
                        tint = Color.White, 
                        modifier = Modifier.size(20.dp).rotate(rotationAnimation)
                    )
                    
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

                    Icon(
                        Icons.Filled.ZoomOut, 
                        "Zoom Out", 
                        tint = Color.White, 
                        modifier = Modifier.size(20.dp).rotate(rotationAnimation)
                    )
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
                                tint = Color.White,
                                modifier = Modifier.rotate(rotationAnimation)
                            )
                        }
                        
                         IconButton(onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK
                        }) {
                            Icon(
                                Icons.Filled.Cameraswitch, 
                                contentDescription = "Switch Camera", 
                                tint = Color.White, 
                                modifier = Modifier.rotate(rotationAnimation) 
                            )
                        }

                        IconButton(onClick = { showFilterMenu = !showFilterMenu }) {
                            Icon(
                                Icons.Filled.AutoAwesome, 
                                contentDescription = "Filters", 
                                tint = if(showFilterMenu || selectedFilter != TextureRenderer.FilterType.NORMAL) Color.Yellow else Color.White,
                                modifier = Modifier.rotate(rotationAnimation)
                            )
                        }
                    }

                    // CENTER: Shutter & Mode
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(64.dp)) {
                        // Compact Mode Switch
                        ModeSelector(
                            currentMode = cameraMode,
                            onModeSelected = { cameraMode = it },
                            rotation = rotationAnimation
                        )

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
                                    modifier = Modifier.fillMaxSize().rotate(rotationAnimation)
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
                                modifier = Modifier.fillMaxSize().clip(CircleShape).rotate(rotationAnimation)
                            )
                        }
                    }
                }
            }
        }
            
        // Filter Menu Overlay
        if (showFilterMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp), // Position above shutter
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextureRenderer.FilterType.values().forEach { filter ->
                        FilterChip(
                            name = filter.name,
                            isSelected = selectedFilter == filter,
                            onClick = { selectedFilter = filter }
                        )
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
fun FilterChip(name: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(name, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
fun ModeSelector(
    currentMode: Int,
    onModeSelected: (Int) -> Unit,
    rotation: Float
) {
    Box(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(30.dp))
            .padding(4.dp)
            .rotate(rotation)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo Option
            ModeIcon(
                icon = Icons.Filled.PhotoCamera,
                isSelected = currentMode == 0,
                onClick = { onModeSelected(0) }
            )
            
            // Video Option
            ModeIcon(
                icon = Icons.Filled.Videocam,
                isSelected = currentMode == 1,
                onClick = { onModeSelected(1) }
            )
        }
    }
}

@Composable
fun ModeIcon(
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color.White else Color.Transparent
    val contentColor = if (isSelected) Color.Black else Color.White
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
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
