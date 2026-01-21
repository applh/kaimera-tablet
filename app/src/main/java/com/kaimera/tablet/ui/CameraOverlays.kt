package com.kaimera.tablet.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate

@Composable
fun CameraOverlays(
    gridRows: Int,
    gridCols: Int,
    circleRadiusPercent: Int,
    isLevel: Boolean,
    rotationAngle: Float,
    focusPoint: Offset?
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Focus Indicator
        focusPoint?.let { offset ->
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(Color.Yellow, radius = 30f, center = offset, style = Stroke(width = 3f))
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

        // Center Circle & Crosshairs Overlay
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

                // Draw Circle
                drawCircle(
                    color = overlayColor,
                    radius = radius,
                    center = Offset(centerX, centerY),
                    style = strokeStyle
                )

                // Draw Crosshairs (Rotated)
                rotate(degrees = rotationAngle, pivot = Offset(centerX, centerY)) {
                    val longDimension = maxDimension * 2f

                    // Horizontal
                    drawLine(
                        color = overlayColor,
                        start = Offset(centerX - radius, centerY),
                        end = Offset(centerX - longDimension, centerY),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = overlayColor,
                        start = Offset(centerX + radius, centerY),
                        end = Offset(centerX + longDimension, centerY),
                        strokeWidth = 2f
                    )

                    // Vertical
                    drawLine(
                        color = overlayColor,
                        start = Offset(centerX, centerY - radius),
                        end = Offset(centerX, centerY - longDimension),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = overlayColor,
                        start = Offset(centerX, centerY + radius),
                        end = Offset(centerX, centerY + longDimension),
                        strokeWidth = 2f
                    )
                }
            }
        }
    }
}
