package com.kaimera.tablet.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class Attachment(
    val type: AttachmentType,
    val name: String,
    val uri: String
)

enum class AttachmentType {
    FILE, NOTE, URL
}

@Composable
fun InputAttachment(
    attachments: List<Attachment>,
    onAddAttachment: (Attachment) -> Unit,
    onRemoveAttachment: (Attachment) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Attachment List
        if (attachments.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                attachments.forEach { item ->
                    InputChip(
                        selected = true,
                        onClick = { /* Preview? */ },
                        label = { Text(item.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = when (item.type) {
                                    AttachmentType.FILE -> Icons.Default.AttachFile
                                    AttachmentType.NOTE -> Icons.Default.Description
                                    AttachmentType.URL -> Icons.Default.Link
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                modifier = Modifier.size(16.dp).clickable { onRemoveAttachment(item) }
                            )
                        }
                    )
                }
            }
        }

        // Add Button
        Box {
            TextButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.AttachFile, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Attachment")
            }
            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("File") },
                    onClick = {
                        showMenu = false
                        onAddAttachment(Attachment(AttachmentType.FILE, "spec_v1.pdf", "file://dummy"))
                    },
                    leadingIcon = { Icon(Icons.Default.AttachFile, null) }
                )
                DropdownMenuItem(
                    text = { Text("Note") },
                    onClick = {
                        showMenu = false
                        onAddAttachment(Attachment(AttachmentType.NOTE, "Meeting Notes", "note://123"))
                    },
                    leadingIcon = { Icon(Icons.Default.Description, null) }
                )
                DropdownMenuItem(
                    text = { Text("URL") },
                    onClick = {
                        showMenu = false
                        onAddAttachment(Attachment(AttachmentType.URL, "Figma Design", "https://figma.com/file"))
                    },
                    leadingIcon = { Icon(Icons.Default.Link, null) }
                )
            }
        }
    }
}
