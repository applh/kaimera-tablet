package com.kaimera.tablet.features.projects.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.kaimera.tablet.core.ui.components.Attachment
import com.kaimera.tablet.core.ui.components.InputAttachment

@Composable
fun AddSpaceDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Space") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Space Name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddProjectDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Project") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Project Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, description) },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Long?) -> Unit // title, assignee, deadline
) {
    var title by remember { mutableStateOf("") }
    var assignee by remember { mutableStateOf<String?>(null) }
    var deadline by remember { mutableStateOf<Long?>(null) }
    val attachments = remember { mutableStateListOf<Attachment>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Assignee & Deadline Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Mock Assignee Selector
                    OutlinedButton(
                        onClick = { assignee = if (assignee == null) "Alice" else null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(assignee ?: "Assignee")
                    }
                    
                    // Mock Deadline Selector
                    OutlinedButton(
                        onClick = { deadline = if (deadline == null) System.currentTimeMillis() else null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (deadline != null) "Today" else "Deadline")
                    }
                }
                
                HorizontalDivider()
                
                Text("Attachments", style = MaterialTheme.typography.labelMedium)
                InputAttachment(
                    attachments = attachments,
                    onAddAttachment = { attachments.add(it) },
                    onRemoveAttachment = { attachments.remove(it) }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, assignee, deadline) },
                enabled = title.isNotBlank()
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
