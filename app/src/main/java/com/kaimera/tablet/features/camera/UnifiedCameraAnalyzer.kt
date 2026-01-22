package com.kaimera.tablet.features.camera

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class UnifiedCameraAnalyzer(
    private val scanQrCodes: Boolean,
    private val aiSceneDetection: Boolean,
    private val onQrCodeDetected: (String) -> Unit,
    private val onSceneDetected: (String?, String?, Float) -> Unit
) : ImageAnalysis.Analyzer {

    private val qrScanner = if (scanQrCodes) {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )
    } else null

    private val sceneLabeler = if (aiSceneDetection) {
        ImageLabeling.getClient(
            ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.7f)
                .build()
        )
    } else null

    private var lastSceneAnalysisTime = 0L

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val currentTime = System.currentTimeMillis()
        val shouldAnalyzeScene = aiSceneDetection && (currentTime - lastSceneAnalysisTime >= 500L)
        val shouldScanQr = scanQrCodes

        if (!shouldAnalyzeScene && !shouldScanQr) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

        // 1. QR Scanning
        val qrTask = if (shouldScanQr) {
            qrScanner?.process(inputImage)?.addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { onQrCodeDetected(it) }
            }
        } else null
        qrTask?.let { tasks.add(it) }

        // 2. Scene Detection (Throttled)
        val sceneTask = if (shouldAnalyzeScene) {
            lastSceneAnalysisTime = currentTime
            sceneLabeler?.process(inputImage)?.addOnSuccessListener { labels ->
                val topLabel = labels.firstOrNull()
                val scene = topLabel?.let { mapLabelToScene(it.text) }
                onSceneDetected(scene, topLabel?.text, topLabel?.confidence ?: 0f)
            }
        } else null
        sceneTask?.let { tasks.add(it) }

        // 3. Finalize
        if (tasks.isEmpty()) {
            imageProxy.close()
        } else {
            Tasks.whenAllComplete(tasks).addOnCompleteListener {
                imageProxy.close()
            }
        }
    }

    private fun mapLabelToScene(label: String): String {
        return when (label.lowercase()) {
            "plant", "flower", "tree", "grass", "nature", "mountain" -> "🌿 Nature"
            "food", "dish", "drink", "cuisine", "tableware" -> "🍽️ Food"
            "text", "font", "document", "handwriting", "newspaper" -> "📄 Document"
            "person", "face", "smile", "man", "woman" -> "👤 Portrait"
            "building", "city", "street", "architecture" -> "🏙️ City"
            "cat", "dog", "animal", "pet" -> "🐾 Animal"
            else -> label.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}
