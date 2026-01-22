package com.kaimera.tablet.features.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaimera.tablet.core.ui.components.TreeNode
import com.kaimera.tablet.core.ui.components.TreePanel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val treeNodes = remember {
        listOf(
            TreeNode("personal", "Personal", Icons.Default.Person),
            TreeNode("work", "Work", Icons.Default.Work),
            TreeNode("shared", "Shared", Icons.Default.Group)
        )
    }
    var selectedNodeId by remember { mutableStateOf("personal") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Calendar", style = MaterialTheme.typography.headlineSmall)
                        Spacer(modifier = Modifier.width(32.dp))
                        IconButton(onClick = { /* Prev Month */ }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        }
                        Text(
                            text = "January 2026",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { /* Next Month */ }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TreePanel(
                nodes = treeNodes,
                selectedNodeId = selectedNodeId,
                onNodeSelected = { selectedNodeId = it.id },
                modifier = Modifier.width(220.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(16.dp)
            ) {
                Column {
                    // Weekday Headers
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Calendar Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items((1..31).toList()) { day ->
                            CalendarDayCell(
                                day = day,
                                isToday = day == 22,
                                isSelected = day == 22,
                                hasEvents = day % 5 == 0,
                                calendarType = selectedNodeId
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasEvents: Boolean,
    calendarType: String
) {
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    }

    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        isToday -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f)
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(8.dp)
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier.align(Alignment.TopStart)
            )

            if (hasEvents) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp)
                )
            }

            if (isToday) {
                Text(
                    text = "TODAY",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}
