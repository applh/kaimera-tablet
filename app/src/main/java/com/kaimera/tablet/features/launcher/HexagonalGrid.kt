package com.kaimera.tablet.features.launcher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

/**
 * A Shape representing a regular pointy-topped hexagon.
 */
class HexagonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height
            
            // Pointy-topped hexagon points
            // Start from top center
            moveTo(width / 2f, 0f)
            lineTo(width, height * 0.25f)
            lineTo(width, height * 0.75f)
            lineTo(width / 2f, height)
            lineTo(0f, height * 0.75f)
            lineTo(0f, height * 0.25f)
            close()
        }
        return Outline.Generic(path)
    }
}

/**
 * A layout that arranges items in a hexagonal honeycomb pattern.
 *
 * @param items The list of items to display.
 * @param columns The number of items in the odd rows (even rows will have columns - 1).
 * @param itemSize The size (diameter) of each hexagon item usually.
 * @param spacing The spacing between items.
 * @param itemContent The composable to render for each item.
 */
@Composable
fun <T> HexagonalGrid(
    items: List<T>,
    columns: Int = 3,
    itemSize: Dp = 120.dp,
    spacing: Dp = 8.dp,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit
) {
    // We break the items into rows.
    // Row 1: N items
    // Row 2: N-1 items (indented)
    // Row 3: N items
    // etc.
    
    // Pattern calculates total items per cycle (2 rows) = columns + (columns - 1) = 2*columns - 1
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var currentIndex = 0
        var rowIndex = 0
        
        while (currentIndex < items.size) {
            val isOddRow = rowIndex % 2 == 0 // 0-indexed, so 0 is 1st row (Odd visual row), 1 is 2nd row (Even visual row)
            val itemsInThisRow = if (isOddRow) columns else columns - 1
            
            // Collect items for this row (don't go out of bounds)
            val endIndex = minOf(currentIndex + itemsInThisRow, items.size)
            val rowItems = items.subList(currentIndex, endIndex)
            
            if (rowItems.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing),
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Center the row manually since we might have fewer items than full width
                    Spacer(modifier = Modifier.weight(1f))
                    
                    rowItems.forEach { item ->
                        Box(contentAlignment = Alignment.Center) {
                            itemContent(item)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                }
                
                // Vertical overlap logic
                // For regular hexagons, the vertical distance between rows in a honeycomb
                // is 3/4 * height. So we need to pull the next row up by 1/4 * height.
                // However, our Row layout puts them nicely one after another.
                // We want to negative spacing to pull them up.
                // height is itemSize (roughly, assuming square box for hexagon).
                // pulling up by diameter/4 is standard for close packing.
                
                // But we also need to account for the 'spacing' parameter.
                
                // Let's create a negative spacer if it's not the last row
                if (endIndex < items.size) {
                   Spacer(modifier = Modifier.height(-itemSize / 5)) // Approximate overlap for visual nesting
                }
            }
            
            currentIndex = endIndex
            rowIndex++
        }
    }
}
