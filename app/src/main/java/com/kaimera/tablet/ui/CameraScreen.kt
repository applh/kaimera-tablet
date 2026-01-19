package com.kaimera.tablet.ui

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import coil.compose.AsyncImage
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CameraScreen(onNavigateToGallery: () -> Unit = {}) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    if (hasCameraPermission) {
        CameraContent(context, lifecycleOwner, onNavigateToGallery)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission is required to use this feature.")
        }
    }
}

@Composable
fun CameraContent(
    context: Context, 
    lifecycleOwner: LifecycleOwner,
    onNavigateToGallery: () -> Unit
) {
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var zoomRatio by remember { mutableFloatStateOf(1f) }
    var maxZoomRatio by remember { mutableFloatStateOf(1f) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val preview = remember { Preview.Builder().build() }
    
    // We need a reference to the camera to set zoom
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }
    var cameraInfo by remember { mutableStateOf<androidx.camera.core.CameraInfo?>(null) }

    // Last captured image URI for thumbnail
    var lastCapturedUri by remember { mutableStateOf<Uri?>(null) }

    // Load last image on start
    LaunchedEffect(Unit) {
        lastCapturedUri = loadLastImage(context)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                previewView.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    
                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(lensFacing)
                        .build()

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                        cameraControl = camera.cameraControl
                        cameraInfo = camera.cameraInfo
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                        
                        // Observe Zoom State
                        cameraInfo?.zoomState?.observe(lifecycleOwner) { state ->
                            zoomRatio = state.zoomRatio
                            maxZoomRatio = state.maxZoomRatio
                        }
                    } catch (exc: Exception) {
                        Log.e("CameraScreen", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize(),
            update = { 
                // In a real app we might update bindings here if lensFacing changes
            }
        )
        
        // Re-bind camera when lensFacing changes
        LaunchedEffect(lensFacing) {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()
                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageCapture
                    )
                    cameraControl = camera.cameraControl
                    cameraInfo = camera.cameraInfo
                } catch (exc: Exception) {
                     Log.e("CameraScreen", "Camera rebind failed", exc)
                }
            }, ContextCompat.getMainExecutor(context))
        }

        // Overlay UI
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Controls (Flash, Grid placeholder) - Kept at top center or left? 
            // User said "system icons toolbar shows at the bottom... avoid this area".
            // Let's put them at the top center for now, horizontal.
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Flash: Auto", color = Color.White)
                // Spacer(modifier = Modifier.width(16.dp)) 
                // Text("HDR: On", color = Color.White)
            }

            // Right Side Controls
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp), // Add some padding from the edge
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Zoom Control
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1fx", zoomRatio),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Zoom Slider (Vertical)
                    // We use BoxWithConstraints to get the available height for the 30% requirement 
                    // relative to the screen/parent, but here we are in a Row, so fillMaxHeight might not work 
                    // as expected if the Row is centered.
                    // Better to just set the height of this container to a fixed 30% of screen height
                    // or just use a weight if the parent column was full height.
                    // Since parent is Box.fillMaxSize -> Row.align(CenterEnd), the Row wraps content.
                    // We can use LocalConfiguration to get screen height or just use a fixed large size
                    // approximating typical tablet use, OR use fillMaxHeight(0.3f) on this specific element if we wrap it in a box that fills height.
                    
                    // Simpler approach: Fixed height that looks like ~30% on tablet (approx 300-400dp).
                    // Or properly: BoxWithConstraints on the top level box to pass height down. 
                    // But for this change, let's use a reasonably large fixed height or try relative.
                    // Let's use 300.dp as a safe bet for "30% of a tablet height" (800dp - 1200dp).
                    
                    // Zoom Slider (Vertical)
                    val configuration = LocalConfiguration.current
                    val screenHeight = configuration.screenHeightDp.dp
                    val sliderHeight = screenHeight * 0.3f

                    Text(
                        text = String.format(Locale.US, "%.1fx", maxZoomRatio),
                        color = Color.White, 
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(bottom=4.dp)
                    )

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(40.dp)
                            .height(sliderHeight) // Dynamic height
                    ) {
                        Slider(
                            value = zoomRatio,
                            onValueChange = { cameraControl?.setZoomRatio(it) },
                            valueRange = 1f..maxZoomRatio,
                            modifier = Modifier
                                .graphicsLayer {
                                    rotationZ = -90f
                                }
                                .requiredWidth(sliderHeight) // Use requiredWidth to ignore parent constraint
                        )
                    }
                    
                    Text(
                        text = "1x", 
                        color = Color.White, 
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top=4.dp)
                    )
                }

                Spacer(modifier = Modifier.width(24.dp))

                // Action Buttons Column
                Column(
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Gallery Thumbnail
                    IconButton(
                        onClick = { onNavigateToGallery() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (lastCapturedUri != null) {
                            AsyncImage(
                                model = lastCapturedUri,
                                contentDescription = "Gallery",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha=0.5f), CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                            )
                        }
                    }

                    // Shutter Button
                    FloatingActionButton(
                        onClick = { 
                            takePhoto(imageCapture, context) { uri ->
                                lastCapturedUri = uri
                            } 
                        },
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, "Take Photo")
                    }

                    // Switch Camera
                    IconButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        }
                    ) {
                        Icon(Icons.Default.Cameraswitch, "Switch Camera", tint = Color.White, modifier = Modifier.size(32.dp))
                    }
                }
            }
        }
    }
}

private fun takePhoto(
    imageCapture: ImageCapture, 
    context: Context,
    onImageSaved: (Uri) -> Unit
) {
    val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
        .format(System.currentTimeMillis())
    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
        put(android.provider.MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Kaimera")
    }

    val outputOptions = ImageCapture.OutputFileOptions.Builder(
        context.contentResolver,
        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    ).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exc: ImageCaptureException) {
                Log.e("CameraScreen", "Photo capture failed: ${exc.message}", exc)
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val msg = "Photo capture succeeded: ${output.savedUri}"
                Log.d("CameraScreen", msg)
                output.savedUri?.let { onImageSaved(it) }
            }
        }
    )
}

private fun loadLastImage(context: Context): Uri? {
    val collection = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(android.provider.MediaStore.Images.Media._ID)
    val sortOrder = "${android.provider.MediaStore.Images.Media.DATE_ADDED} DESC"
    val selection = "${android.provider.MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
    val selectionArgs = arrayOf("Pictures/Kaimera%")

    context.contentResolver.query(
        collection,
        projection,
        selection, // Filter for app specific folder or remove to show all
        selectionArgs,
        sortOrder
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val idColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media._ID)
            val id = cursor.getLong(idColumn)
            return ContentUris.withAppendedId(collection, id)
        }
    }
    // Fallback: try querying all images if Kaimera specific query fails or is empty initially
     context.contentResolver.query(
        collection,
        projection,
        null, 
        null,
        sortOrder
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val idColumn = cursor.getColumnIndexOrThrow(android.provider.MediaStore.Images.Media._ID)
            val id = cursor.getLong(idColumn)
            return ContentUris.withAppendedId(collection, id)
        }
    }
    return null
}
