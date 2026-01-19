package com.kaimera.tablet.ui

import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.util.Log
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.ExposureState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CameraViewModel : ViewModel() {

    // Exposure
    private val _exposureIndex = MutableStateFlow(0)
    val exposureIndex: StateFlow<Int> = _exposureIndex.asStateFlow()

    private val _exposureRange = MutableStateFlow(0..0)
    val exposureRange: StateFlow<IntRange> = _exposureRange.asStateFlow()

    private val _exposureStep = MutableStateFlow(0f)
    // val exposureStep: StateFlow<Float> = _exposureStep.asStateFlow() // Not strictly needed for UI if we just step indices

    // White Balance
    private val _whiteBalanceMode = MutableStateFlow(CameraMetadata.CONTROL_AWB_MODE_AUTO)
    val whiteBalanceMode: StateFlow<Int> = _whiteBalanceMode.asStateFlow()

    // Monitoring
    private val _isoValue = MutableStateFlow(0)
    val isoValue: StateFlow<Int> = _isoValue.asStateFlow()

    private val _shutterSpeed = MutableStateFlow(0L)
    val shutterSpeed: StateFlow<Long> = _shutterSpeed.asStateFlow()

    // Advanced Modes State
    enum class CaptureState { IDLE, BURSTING, TIMELAPSE }

    private val _captureState = MutableStateFlow(CaptureState.IDLE)
    val captureState: StateFlow<CaptureState> = _captureState.asStateFlow()

    private val _capturedCount = MutableStateFlow(0)
    val capturedCount: StateFlow<Int> = _capturedCount.asStateFlow()

    private var captureJob: Job? = null

    private var currentCameraControl: CameraControl? = null
    private var currentCameraInfo: CameraInfo? = null

    fun bindCamera(camera: Camera) {
        currentCameraControl = camera.cameraControl
        currentCameraInfo = camera.cameraInfo
        
        // Initialize Exposure Constraints
        currentCameraInfo?.exposureState?.let { state ->
            val range = state.exposureCompensationRange
            _exposureRange.value = range.lower..range.upper
            _exposureStep.value = state.exposureCompensationStep.toFloat()
            _exposureIndex.value = state.exposureCompensationIndex
        }

        // Setup Monitoring Callback via Camera2Interop
        // Function to register callback if possible
        setupCamera2Callback(camera)
    }

    private fun setupCamera2Callback(camera: Camera) {
        try {
            val camera2Info = Camera2CameraInfo.from(camera.cameraInfo)
            // CameraX doesn't easily expose a continuous capture callback for every frame to reading metadata
            // officially. However, we can inspect capabilities. 
            // Real-time ISO/Shutter monitoring is TRICKY in pure CameraX without using an Analyzer or 
            // hooking into CaptureResult, which isn't directly exposed nicely.
            // 
            // Workaround: We might only be able to set values, not read them live easily without
            // a custom LifecycleController or heavy hack.
            //
            // BETTER APPROACH for MVP: Focus on CONTROLS (Setting values).
            // Reading live ISO is a "Nice to have" that might require an ImageAnalysis that checks metadata 
            // if the ImageProxy exposes it.
            
            // Actually, we can assume Auto is working. 
            // For now, let's implement the SETTERS first.
        } catch (e: Exception) {
            Log.e("CameraViewModel", "Failed to access Camera2Info", e)
        }
    }

    fun setExposureCompensation(index: Int) {
        currentCameraControl?.setExposureCompensationIndex(index)?.addListener({
            _exposureIndex.value = index
        }, androidx.camera.core.impl.utils.executor.CameraXExecutors.directExecutor())
    }

    fun setWhiteBalance(mode: Int) {
        _whiteBalanceMode.value = mode
        
        // Apply via Camera2Interop
        currentCameraControl?.let { control ->
            val camera2Control = Camera2CameraControl.from(control)
            val captureRequestOptions = CaptureRequestOptions.Builder()
                .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_MODE, mode)
                .build()
            
            camera2Control.setCaptureRequestOptions(captureRequestOptions)
        }
    }
    
    // Helper for WB Labels
    fun getWhiteBalanceLabel(mode: Int): String {
        return when (mode) {
            CameraMetadata.CONTROL_AWB_MODE_AUTO -> "Auto"
            CameraMetadata.CONTROL_AWB_MODE_CLOUDY_DAYLIGHT -> "Cloudy"
            CameraMetadata.CONTROL_AWB_MODE_DAYLIGHT -> "Sunny"
            CameraMetadata.CONTROL_AWB_MODE_INCANDESCENT -> "Incandescent"
            CameraMetadata.CONTROL_AWB_MODE_FLUORESCENT -> "Fluorescent"
            else -> "Auto"
        }
    }

    // --- Advanced Modes Logic ---

    fun startBurst(onCapture: suspend () -> Unit) {
        if (_captureState.value != CaptureState.IDLE) return
        _captureState.value = CaptureState.BURSTING
        _capturedCount.value = 0
        captureJob = viewModelScope.launch {
            while (isActive) {
                try {
                    onCapture()
                    _capturedCount.value++
                } catch (e: Exception) {
                    Log.e("CameraViewModel", "Burst capture failed", e)
                }
                delay(50) // Reduced delay since we now wait for capture to complete. 
                         // 50ms + CaptureTime ensures we don't overwhelm.
            }
        }
    }

    fun stopBurst() {
        if (_captureState.value == CaptureState.BURSTING) {
            captureJob?.cancel()
            _captureState.value = CaptureState.IDLE
        }
    }

    fun startTimelapse(intervalMillis: Long, onCapture: suspend () -> Unit) {
        if (_captureState.value != CaptureState.IDLE) return
        _captureState.value = CaptureState.TIMELAPSE
        _capturedCount.value = 0
        captureJob = viewModelScope.launch {
            while (isActive) {
                try {
                    onCapture()
                    _capturedCount.value++
                } catch (e: Exception) {
                     Log.e("CameraViewModel", "Timelapse capture failed", e)
                }
                delay(intervalMillis)
            }
        }
    }

    fun stopTimelapse() {
        if (_captureState.value == CaptureState.TIMELAPSE) {
            captureJob?.cancel()
            _captureState.value = CaptureState.IDLE
        }
    }
}
