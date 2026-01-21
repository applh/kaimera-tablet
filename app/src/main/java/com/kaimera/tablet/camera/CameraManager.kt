package com.kaimera.tablet.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraManager(private val context: Context) {

    private val cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private val _zoomState = MutableStateFlow(1f)
    val zoomState: StateFlow<Float> = _zoomState.asStateFlow()

    private val _maxZoomState = MutableStateFlow(1f)
    val maxZoomState: StateFlow<Float> = _maxZoomState.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _flashMode = MutableStateFlow(ImageCapture.FLASH_MODE_OFF)
    val flashMode: StateFlow<Int> = _flashMode.asStateFlow()

    private val _recordingDurationNanos = MutableStateFlow(0L)
    val recordingDurationNanos: StateFlow<Long> = _recordingDurationNanos.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _exposureIndex = MutableStateFlow(0)
    val exposureIndex: StateFlow<Int> = _exposureIndex.asStateFlow()

    private val _exposureRange = MutableStateFlow(android.util.Range(0, 0))
    val exposureRange: StateFlow<android.util.Range<Int>> = _exposureRange.asStateFlow()

    private val _exposureStep = MutableStateFlow(android.util.Rational.ZERO)
    val exposureStep: StateFlow<android.util.Rational> = _exposureStep.asStateFlow()

    init {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
        }, ContextCompat.getMainExecutor(context))
    }

    fun bindVideoPreview(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        videoResolutionTier: Int = 1, // 0: HD, 1: FHD, 2: 4K
        targetFps: Int = 30
    ) {
        val provider = cameraProvider ?: return

        val quality = when (videoResolutionTier) {
            0 -> Quality.HD
            2 -> Quality.UHD
            else -> Quality.FHD
        }

        val selector = QualitySelector.from(
            quality,
            FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
        )
        val recorder = Recorder.Builder()
            .setQualitySelector(selector)
            .build()
        videoCapture = VideoCapture.withOutput(recorder)

        val preview = Preview.Builder().build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                videoCapture
            )
            observeCameraState(lifecycleOwner)
        } catch (e: Exception) {
            Log.e("CameraManager", "Binding failed", e)
        }
    }

    fun bindPhotoPreview(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        flashMode: Int = ImageCapture.FLASH_MODE_OFF,
        photoResolutionTier: Int = 1,
        jpegQuality: Int = 95,
        captureMode: Int = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY
    ) {
        val provider = cameraProvider ?: return

        _flashMode.value = flashMode

        val resolutionStrategy = when (photoResolutionTier) {
            0 -> ResolutionStrategy(android.util.Size(1280, 720), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
            2 -> ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY
            else -> ResolutionStrategy(android.util.Size(1920, 1080), ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
        }
        val resolutionSelector = androidx.camera.core.resolutionselector.ResolutionSelector.Builder()
            .setResolutionStrategy(resolutionStrategy)
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .build()
        preview.setSurfaceProvider(previewView.surfaceProvider)

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(captureMode)
            .setResolutionSelector(resolutionSelector)
            .setFlashMode(flashMode)
            .setJpegQuality(jpegQuality)
            .build()

        try {
            provider.unbindAll()
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            observeCameraState(lifecycleOwner)
        } catch (e: Exception) {
            Log.e("CameraManager", "Binding failed", e)
        }
    }

    private fun observeCameraState(lifecycleOwner: LifecycleOwner) {
        camera?.cameraInfo?.zoomState?.observe(lifecycleOwner) { state ->
            _zoomState.value = state.zoomRatio
            _maxZoomState.value = state.maxZoomRatio
        }
        // ExposureState is not LiveData, read initial values
        camera?.cameraInfo?.exposureState?.let { state ->
            _exposureIndex.value = state.exposureCompensationIndex
            _exposureRange.value = state.exposureCompensationRange
            _exposureStep.value = state.exposureCompensationStep
        }
    }

    fun setZoom(ratio: Float) {
        camera?.cameraControl?.setZoomRatio(ratio)
    }

    fun setExposureCompensationIndex(index: Int) {
        camera?.cameraControl?.setExposureCompensationIndex(index)
        _exposureIndex.value = index
    }

    fun setFlashMode(mode: Int) {
        _flashMode.value = mode
        imageCapture?.flashMode = mode
    }

    fun focus(previewView: PreviewView, x: Float, y: Float) {
        val factory = previewView.meteringPointFactory
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point).build()
        camera?.cameraControl?.startFocusAndMetering(action)
    }

    fun takePhoto(onPhotoSaved: (Uri) -> Unit) {
        val imageCapture = imageCapture ?: return

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

        imageCapture.takePicture(
            outputOptions,
            cameraExecutor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    outputFileResults.savedUri?.let { onPhotoSaved(it) }
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.e("CameraManager", "Photo capture failed", exception)
                }
            }
        )
    }

    fun startVideoRecording(onVideoSaved: (Uri) -> Unit) {
        val videoCapture = videoCapture ?: return

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
        ).setContentValues(contentValues).build()

        recording = videoCapture.output
            .prepareRecording(context, mediaStoreOutputOptions)
            .apply {
                // Audio recording requires permission check usually, but for now we simplify
            }
            .start(cameraExecutor) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        _isRecording.value = true
                        _isPaused.value = false
                        _recordingDurationNanos.value = 0L
                    }
                    is VideoRecordEvent.Pause -> {
                        _isPaused.value = true
                    }
                    is VideoRecordEvent.Resume -> {
                        _isPaused.value = false
                    }
                    is VideoRecordEvent.Status -> {
                        // Only update duration if not paused?
                        // Actually duration tracking might update during pause but stats.recordedDurationNanos should represent active recording
                        _recordingDurationNanos.value = event.recordingStats.recordedDurationNanos
                    }
                    is VideoRecordEvent.Finalize -> {
                        _isRecording.value = false
                        _isPaused.value = false
                        _recordingDurationNanos.value = 0L
                        if (!event.hasError()) {
                            onVideoSaved(event.outputResults.outputUri)
                        }
                    }
                }
            }
    }

    fun stopVideoRecording() {
        recording?.stop()
        recording = null
    }

    fun pauseVideoRecording() {
        if (_isRecording.value && !_isPaused.value) {
            recording?.pause()
        }
    }

    fun resumeVideoRecording() {
        if (_isRecording.value && _isPaused.value) {
            recording?.resume()
        }
    }

    fun release() {
        cameraExecutor.shutdown()
    }
}
