package com.kaimera.tablet.files

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage

@Composable
fun FilesScreen(onFileOpen: (MediaFile) -> Unit = {}) {
    val context = LocalContext.current
    val viewModel: FilesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FilesViewModel::class.java)) {
                    return FilesViewModel(FilesRepositoryImpl(context)) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )

    val uiState by viewModel.uiState.collectAsState()
    val actionEffect by viewModel.actionEffect.collectAsState()

    // Handle Delete Permission Request
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
             viewModel.loadData()
        }
    }

    LaunchedEffect(actionEffect) {
        val effect = actionEffect
        if (effect is FilesActionEffect.RequestDeletePermission) {
            val intentSenderRequest = androidx.activity.result.IntentSenderRequest.Builder(effect.intentSender).build()
            launcher.launch(intentSenderRequest)
            viewModel.clearEffect()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        when (val state = uiState) {
            is FilesUiState.Loading -> {
                CircularProgressIndicator()
            }
            is FilesUiState.Success -> {
                if (state.media.isEmpty()) {
                    Text(
                        text = "No images found",
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        contentPadding = PaddingValues(4.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.media) { media ->
                            MediaItem(
                                media = media,
                                onOpen = { onFileOpen(media) },
                                onDelete = { viewModel.deleteFile(media) },
                                onRename = { newName -> viewModel.renameFile(media, newName) }
                            )
                        }
                    }
                }
            }
            is FilesUiState.Error -> {
                Text(text = "Error: ${state.message}")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MediaItem(
    media: MediaFile,
    onOpen: () -> Unit,
    onDelete: () -> Unit,
    onRename: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    if (showRenameDialog) {
        var newName by remember { mutableStateOf(media.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename") },
            text = {
                TextField(value = newName, onValueChange = { newName = it })
            },
            confirmButton = {
                Button(onClick = {
                    onRename(newName)
                    showRenameDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                Button(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }

    Box(
        modifier = Modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .combinedClickable(
                onClick = onOpen,
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showMenu = true
                }
            )
    ) {
        AsyncImage(
            model = media.uri,
            contentDescription = media.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        
        // Duration Overlay (Top Right)
        if (media.isVideo && media.duration > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = formatDuration(media.duration),
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Bottom Metadata Overlay
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(4.dp)
        ) {
            Column {
                Text(
                    text = media.name,
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatFileSize(media.size),
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall
                    )
                    if (media.width > 0 && media.height > 0) {
                        Text(
                            text = "${media.width}x${media.height}",
                            color = Color.White.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                Text(
                    text = formatDate(media.dateAdded),
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        if (media.isVideo) {
            Icon(
                imageVector = Icons.Default.PlayCircleOutline,
                contentDescription = "Video",
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp)
            )
        }

        // Context Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Rename") },
                onClick = {
                    showMenu = false
                    showRenameDialog = true
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    showMenu = false
                    onDelete()
                }
            )
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val seconds = (durationMs / 1000) % 60
    val minutes = (durationMs / (1000 * 60)) % 60
    val hours = durationMs / (1000 * 60 * 60)
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

private fun formatDate(dateAddedSeconds: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(dateAddedSeconds * 1000))
}
