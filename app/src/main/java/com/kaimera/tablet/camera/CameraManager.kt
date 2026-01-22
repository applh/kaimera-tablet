package com.kaimera.tablet.camera

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.*
import androidx.camera.camera2.interop.Camera2Interop
import android.hardware.camera2.CaptureRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.extensions.ExtensionsManager
import androidx.camera.extensions.ExtensionMode
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import kotlinx.coroutines.*
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
    private var extensionsManager: ExtensionsManager? = null
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

    private val _actualResolution = MutableStateFlow(android.util.Size(0, 0))
    val actualResolution: StateFlow<android.util.Size> = _actualResolution.asStateFlow()

    private val _supportedExtensions = MutableStateFlow<List<Int>>(emptyList())
    val supportedExtensions: StateFlow<List<Int>> = _supportedExtensions.asStateFlow()

    private val _detectedQrCode = MutableStateFlow<String?>(null)
    val detectedQrCode: StateFlow<String?> = _detectedQrCode.asStateFlow()

    private val _torchEnabled = MutableStateFlow(false)
    val torchEnabled: StateFlow<Boolean> = _torchEnabled.asStateFlow()

    private val _detectedScene = MutableStateFlow<String?>(null)
    val detectedScene: StateFlow<String?> = _detectedScene.asStateFlow()

    private var imageAnalysis: ImageAnalysis? = null
    
    // Timelapse State
    private var timelapseJob: Job? = null
    private val _isTimelapseActive = MutableStateFlow(false)
    val isTimelapseActive: StateFlow<Boolean> = _isTimelapseActive.asStateFlow()

    private val _timelapseFrames = MutableStateFlow(0)
    val timelapseFrames: StateFlow<Int> = _timelapseFrames.asStateFlow()

    private var timelapseInterval: Long = 2000L

    init {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider = provider
            
            // Initialize ExtensionsManager
            val extensionsManagerFuture = ExtensionsManager.getInstanceAsync(context, provider)
            extensionsManagerFuture.addListener({
                extensionsManager = extensionsManagerFuture.get()
                // Check initial availability for back camera
                checkExtensionAvailability(CameraSelector.LENS_FACING_BACK)
            }, ContextCompat.getMainExecutor(context))
            
        }, ContextCompat.getMainExecutor(context))
    }

    private fun checkExtensionAvailability(lensFacing: Int) {
        val manager = extensionsManager ?: return
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        
        val available = mutableListOf<Int>()
        val modes = listOf(
            ExtensionMode.HDR,
            ExtensionMode.NIGHT,
            ExtensionMode.BOKEH,
            ExtensionMode.FACE_RETOUCH,
            ExtensionMode.AUTO
        )
        
        for (mode in modes) {
            if (manager.isExtensionAvailable(cameraSelector, mode)) {
                available.add(mode)
            }
        }
        _supportedExtensions.value = available
    }

    private val _surfaceRequest = MutableStateFlow<SurfaceRequest?>(null)
    val surfaceRequest: StateFlow<SurfaceRequest?> = _surfaceRequest.asStateFlow()

    // ... (existing flows)

    fun bindVideoPreview(
        lifecycleOwner: LifecycleOwner,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        videoResolutionTier: Int = 1, // 0: HD, 1: FHD, 2: 4K
        targetFps: Int = 30,
        windowSize: android.util.Size,
        scanQrCodes: Boolean = false,
        aiSceneDetection: Boolean = false,
        whiteBalanceMode: Int = CaptureRequest.CONTROL_AWB_MODE_AUTO,
        torchEnabled: Boolean = false
    ) {
        val provider = cameraProvider ?: return
        
        setupImageAnalysis(scanQrCodes, aiSceneDetection)

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

        val windowRatio = windowSize.width.toFloat() / windowSize.height.toFloat()
        val isPortrait = windowSize.width < windowSize.height
        val aspectRatioStrategy = if (Math.abs(windowRatio - (3.0/4.0)) < 0.1 || Math.abs(windowRatio - (4.0/3.0)) < 0.1) {
             AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
        } else {
             AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
        }

        val targetResolution = if (isPortrait) android.util.Size(1080, 1920) else android.util.Size(1920, 1080)
        
        val resolutionSelector = ResolutionSelector.Builder()
             .setAspectRatioStrategy(aspectRatioStrategy)
             .setResolutionStrategy(ResolutionStrategy(targetResolution, ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER))
             .build()

        val previewBuilder = Preview.Builder()
            .setResolutionSelector(resolutionSelector)

        Camera2Interop.Extender(previewBuilder)
            .setCaptureRequestOption(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, android.util.Range(targetFps, targetFps))
            .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, whiteBalanceMode)

        val preview = previewBuilder.build()
        preview.setSurfaceProvider { request -> _surfaceRequest.value = request }

        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            provider.unbindAll()
            val useCases = mutableListOf<UseCase>(preview, videoCapture!!)
            imageAnalysis?.let { useCases.add(it) }
            
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                *useCases.toTypedArray()
            )
            observeCameraState(lifecycleOwner)
            setTorch(torchEnabled)
        } catch (e: Exception) {
            Log.e("CameraManager", "Binding failed", e)
        }
    }

    fun bindPhotoPreview(
        lifecycleOwner: LifecycleOwner,
        lensFacing: Int = CameraSelector.LENS_FACING_BACK,
        flashMode: Int = ImageCapture.FLASH_MODE_OFF,
        photoResolutionTier: Int = 1,
        jpegQuality: Int = 95,
        captureMode: Int = ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY,
        extensionMode: Int = ExtensionMode.NONE,
        windowSize: android.util.Size,
        scanQrCodes: Boolean = false,
        aiSceneDetection: Boolean = false,
        whiteBalanceMode: Int = CaptureRequest.CONTROL_AWB_MODE_AUTO,
        torchEnabled: Boolean = false
    ) {
        val provider = cameraProvider ?: return
        setupImageAnalysis(scanQrCodes, aiSceneDetection)
        checkExtensionAvailability(lensFacing)
        _flashMode.value = flashMode

        val isPortrait = windowSize.width < windowSize.height
        val windowRatio = windowSize.width.toFloat() / windowSize.height.toFloat()
        
        val aspectRatioStrategy = if (Math.abs(windowRatio - 0.75f) < 0.15f || Math.abs(windowRatio - 1.33f) < 0.15f) {
             AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY
        } else {
             AspectRatioStrategy.RATIO_16_9_FALLBACK_AUTO_STRATEGY
        }

        val targetWidth = if (photoResolutionTier == 0) 720 else if (photoResolutionTier == 2) 2160 else 1080
        val targetHeight = if (photoResolutionTier == 0) 1280 else if (photoResolutionTier == 2) 3840 else 1920
        val targetSize = if (isPortrait) android.util.Size(targetWidth, targetHeight) else android.util.Size(targetHeight, targetWidth)

        val resolutionStrategy = ResolutionStrategy(targetSize, ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER)
        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(aspectRatioStrategy)
            .setResolutionStrategy(resolutionStrategy)
            .build()

        val previewBuilder = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
        
        Camera2Interop.Extender(previewBuilder)
            .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, whiteBalanceMode)

        val preview = previewBuilder.build()
        preview.setSurfaceProvider { request -> _surfaceRequest.value = request }

        var cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
            
        if (extensionMode != ExtensionMode.NONE && extensionsManager != null) {
            if (extensionsManager!!.isExtensionAvailable(cameraSelector, extensionMode)) {
                try {
                    cameraSelector = extensionsManager!!.getExtensionEnabledCameraSelector(cameraSelector, extensionMode)
                } catch (e: Exception) {
                    Log.e("CameraManager", "Failed to enable extension $extensionMode", e)
                }
            }
        }

        val imageCaptureBuilder = ImageCapture.Builder()
            .setCaptureMode(captureMode)
            .setResolutionSelector(resolutionSelector)
            .setFlashMode(flashMode)
            .setJpegQuality(jpegQuality)

        Camera2Interop.Extender(imageCaptureBuilder)
            .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, whiteBalanceMode)

        imageCapture = imageCaptureBuilder.build()

        try {
            provider.unbindAll()
            val useCases = mutableListOf<UseCase>(preview, imageCapture!!)
            imageAnalysis?.let { useCases.add(it) }

            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                *useCases.toTypedArray()
            )
            observeCameraState(lifecycleOwner)
            setTorch(torchEnabled)
        } catch (e: Exception) {
            Log.e("CameraManager", "Binding failed", e)
        }
    }

    private fun observeCameraState(lifecycleOwner: LifecycleOwner) {
        camera?.cameraInfo?.zoomState?.observe(lifecycleOwner) { state ->
            _zoomState.value = state.zoomRatio
            _maxZoomState.value = state.maxZoomRatio
        }
        camera?.cameraInfo?.exposureState?.let { state ->
            _exposureIndex.value = state.exposureCompensationIndex
            _exposureRange.value = state.exposureCompensationRange
            _exposureStep.value = state.exposureCompensationStep
        }
        imageCapture?.resolutionInfo?.resolution?.let {
            _actualResolution.value = it
        }
    }

    fun setZoomRatio(ratio: Float) {
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

    private fun setupImageAnalysis(scanQrCodes: Boolean, aiSceneDetection: Boolean) {
        if (scanQrCodes || aiSceneDetection) {
            val qrAnalyzer = if (scanQrCodes) com.kaimera.tablet.camera.QrCodeAnalyzer { qrCode -> 
                _detectedQrCode.value = qrCode 
            } else null
            
            val sceneAnalyzer = if (aiSceneDetection) com.kaimera.tablet.camera.SceneAnalyzer { scene -> 
                _detectedScene.value = scene 
            } else null
            
            imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        qrAnalyzer?.analyze(imageProxy)
                        sceneAnalyzer?.analyze(imageProxy)
                        imageProxy.close()
                    }
                }
        } else {
            imageAnalysis = null
        }
    }

    fun setTorch(enabled: Boolean) {
        camera?.cameraControl?.enableTorch(enabled)
        _torchEnabled.value = enabled
    }

    fun focus(width: Float, height: Float, x: Float, y: Float) {
        val factory = SurfaceOrientedMeteringPointFactory(width, height)
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point).build()
        camera?.cameraControl?.startFocusAndMetering(action)
    }

    fun clearDetectedQrCode() {
        _detectedQrCode.value = null
    }

    fun takePhoto(onPhotoSaved: (Uri) -> Unit) {
        val imageCapture = imageCapture ?: return
        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())
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
        startRecordingInternal(onVideoSaved, isTimelapse = false)
    }

    fun startTimelapseRecording(intervalMs: Long, onVideoSaved: (Uri) -> Unit) {
        this.timelapseInterval = intervalMs
        startRecordingInternal(onVideoSaved, isTimelapse = true, intervalMs = intervalMs)
    }

    fun setTimelapseInterval(intervalMs: Long) {
        Log.d("CameraManager", "Updating timelapse interval to: $intervalMs")
        this.timelapseInterval = intervalMs
    }

    private fun startRecordingInternal(onVideoSaved: (Uri) -> Unit, isTimelapse: Boolean, intervalMs: Long = 0L) {
        val recorder = (videoCapture?.output as? Recorder) ?: return
        _timelapseFrames.value = 0
        val name = "Kaimera_${System.currentTimeMillis()}.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Kaimera")
            }
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        val pendingRecording = recorder.prepareRecording(context, mediaStoreOutputOptions)
        if (!isTimelapse) {
            pendingRecording.withAudioEnabled()
        }
        
        recording = pendingRecording.start(cameraExecutor) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        _isRecording.value = true
                        _isPaused.value = false
                        if (isTimelapse) {
                            _isTimelapseActive.value = true
                            startTimelapsePulse(intervalMs)
                        }
                    }
                    is VideoRecordEvent.Pause -> {
                        _isPaused.value = true
                    }
                    is VideoRecordEvent.Resume -> {
                        _isPaused.value = false
                    }
                    is VideoRecordEvent.Status -> {
                        _recordingDurationNanos.value = event.recordingStats.recordedDurationNanos
                    }
                    is VideoRecordEvent.Finalize -> {
                        _isRecording.value = false
                        _isPaused.value = false
                        _isTimelapseActive.value = false
                        _recordingDurationNanos.value = 0L
                        timelapseJob?.cancel()
                        if (!event.hasError()) {
                            onVideoSaved(event.outputResults.outputUri)
                        }
                    }
                }
            }
    }

    private fun startTimelapsePulse(intervalMs: Long) {
        Log.d("CameraManager", "Starting timelapse pulse loop with interval: $intervalMs")
        timelapseJob?.cancel()
        timelapseJob = MainScope().launch {
            delay(1000) // Initial delay to stabilize
            while (isActive && _isRecording.value) {
                Log.d("CameraManager", "Pulse: RESUME")
                recording?.resume()
                _timelapseFrames.value++
                delay(300) // Increased pulse duration for encoder stability
                
                if (!isActive || !_isRecording.value) break
                
                Log.d("CameraManager", "Pulse: PAUSE")
                recording?.pause()
                delay(timelapseInterval)
            }
            Log.d("CameraManager", "Timelapse pulse loop ended")
        }
    }

    fun stopVideoRecording() {
        timelapseJob?.cancel()
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
