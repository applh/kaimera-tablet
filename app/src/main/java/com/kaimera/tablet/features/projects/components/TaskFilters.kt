package com.kaimera.tablet.features.projects.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFilters(
    onFilterChange: (String?) -> Unit, 
    onSortChange: (String) -> Unit 
) {
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        
        FilterChip(
            selected = selectedFilter == "High",
            onClick = { 
                selectedFilter = if (selectedFilter == "High") null else "High"
                onFilterChange(selectedFilter)
            },
            label = { Text("High Priority") }
        )
        
        FilterChip(
            selected = selectedFilter == "Doing",
            onClick = { 
                 selectedFilter = if (selectedFilter == "Doing") null else "Doing"
                 onFilterChange(selectedFilter)
            },
            label = { Text("In Progress") }
        )

        Spacer(modifier = Modifier.weight(1f))

        Icon(Icons.Default.Sort, contentDescription = "Sort", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        TextButton(onClick = { onSortChange("Date") }) {
            Text("Date")
        }
        TextButton(onClick = { onSortChange("Priority") }) {
            Text("Priority")
        }
    }
}
