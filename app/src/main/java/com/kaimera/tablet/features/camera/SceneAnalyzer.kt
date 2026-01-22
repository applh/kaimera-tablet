package com.kaimera.tablet.features.camera

import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions

class SceneAnalyzer(private val onSceneDetected: (String?) -> Unit) : ImageAnalysis.Analyzer {

    private val labeler = ImageLabeling.getClient(
        ImageLabelerOptions.Builder()
            .setConfidenceThreshold(0.7f)
            .build()
    )

    private var lastAnalysisTime = 0L

    @ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        // Throttle analysis to once every 500ms to save CPU
        if (currentTime - lastAnalysisTime < 500L) {
            imageProxy.close()
            return
        }
        lastAnalysisTime = currentTime

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            
            labeler.process(image)
                .addOnSuccessListener { labels ->
                    val scene = labels.firstOrNull()?.let { label ->
                        mapLabelToScene(label.text)
                    }
                    onSceneDetected(scene)
                }
                .addOnFailureListener { e ->
                    Log.e("SceneAnalyzer", "Labeling failed", e)
                }
                // .addOnCompleteListener { // Caller will close
                //     imageProxy.close()
                // }
        } else {
            // imageProxy.close() // Caller will close
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
            else -> label.replaceFirstChar { it.uppercase() }
        }
    }
}
