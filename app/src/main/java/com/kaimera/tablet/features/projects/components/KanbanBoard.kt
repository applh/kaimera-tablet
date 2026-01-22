package com.kaimera.tablet.features.projects.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kaimera.tablet.data.local.entities.Task
import com.kaimera.tablet.features.projects.TaskCard

@Composable
fun KanbanBoard(
    tasks: List<Task>,
    onStatusChange: (Task, String) -> Unit
) {
    val todoTasks = tasks.filter { it.status == "Todo" }
    val doingTasks = tasks.filter { it.status == "Doing" }
    val doneTasks = tasks.filter { it.status == "Done" }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KanbanColumn(
            title = "To Do",
            tasks = todoTasks,
            modifier = Modifier.weight(1f),
            onTaskDrop = { task -> onStatusChange(task, "Todo") }
        )
        KanbanColumn(
            title = "In Progress",
            tasks = doingTasks,
            modifier = Modifier.weight(1f),
            onTaskDrop = { task -> onStatusChange(task, "Doing") }
        )
        KanbanColumn(
            title = "Done",
            tasks = doneTasks,
            modifier = Modifier.weight(1f),
            onTaskDrop = { task -> onStatusChange(task, "Done") }
        )
    }
}

@Composable
fun KanbanColumn(
    title: String,
    tasks: List<Task>,
    modifier: Modifier = Modifier,
    onTaskDrop: (Task) -> Unit // Simplified placeholder for drop logic
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(tasks) { task ->
                // TODO: Implement actual drag source here
                // For MVP, we will use a simple click menu or long press to move if DnD is too complex without libraries
                // Or we wrap TaskCard in a Draggable
                DraggableTaskCard(task)
            }
        }
    }
}

@Composable
fun DraggableTaskCard(task: Task) {
    // Placeholder for draggable wrapper
    // Actual Drag and Drop in pure Compose without libraries requires specific setup
    // We will stick to displaying the card for now to verify layout
    // We will pass a dummy onStatusChange for the checkbox inside
    com.kaimera.tablet.features.projects.TaskCard(
        task = task,
        onStatusChange = {} // Checkbox disabled in Board view? Or maybe just works
    )
}
