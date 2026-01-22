package com.kaimera.tablet.features.downloads

import com.kaimera.tablet.core.ui.components.TreeNode
import com.kaimera.tablet.core.ui.components.TreePanel
import com.kaimera.tablet.core.ui.components.NavDrawerTreePanel
import kotlinx.coroutines.launch

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    onBack: () -> Unit,
    viewModel: DownloadsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val files by viewModel.files.collectAsStateWithLifecycle()
    val storageInfo by viewModel.storageInfo.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }


    if (showAddDialog) {
        var urlText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Download") },
            text = {
                Column {
                    Text("Enter the URL of the file you want to download:")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = urlText,
                        onValueChange = { urlText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("https://example.com/file.pdf") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (urlText.isNotBlank()) {
                            viewModel.downloadUrl(urlText)
                            showAddDialog = false
                        }
                    }
                ) {
                    Text("Download")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    NavDrawerTreePanel(
        drawerState = drawerState,
        title = "Downloads",
        onHomeClick = onBack,
        nodes = remember {

            listOf(
                TreeNode("all", "All Files", Icons.Default.Folder),
                TreeNode("recent", "Recent", Icons.Default.History),
                TreeNode("images", "Images", Icons.Default.Image),
                TreeNode("videos", "Videos", Icons.Default.VideoLibrary),
                TreeNode("documents", "Documents", Icons.Default.Description),
                TreeNode("others", "Others", Icons.Default.InsertDriveFile)
            )
        },
        selectedNodeId = selectedCategory.id,
        onNodeSelected = { node ->
            val category = DownloadsViewModel.DownloadsCategory.values()
                .find { it.id == node.id } ?: DownloadsViewModel.DownloadsCategory.ALL
            viewModel.onCategorySelected(category)
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Downloads") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },

                    actions = {
                        IconButton(onClick = { viewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                )
            },

        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Download")
            }
        },
        bottomBar = {
            storageInfo?.let { info ->
                BottomAppBar {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Storage Used: ${formatFileSize(info.usedBytes)}",
                                style = MaterialTheme.typography.labelSmall
                            )
                            Text(
                                text = "${(info.usedPercent * 100).toInt()}%",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { info.usedPercent },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

                if (files.isEmpty() && !isRefreshing) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FileDownloadOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No downloads found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(files) { downloadFile ->
                            DownloadItem(
                                file = downloadFile,
                                onOpen = { openFile(context, downloadFile.file) },
                                onDelete = { viewModel.deleteFile(downloadFile) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun DownloadItem(
    file: DownloadFile,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete File") },
            text = { Text("Are you sure you want to delete '${file.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteConfirm = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    ListItem(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        headlineContent = {
            Text(
                text = file.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
        },
        supportingContent = {
            Text(
                text = "${formatFileSize(file.size)} • ${formatDate(file.lastModified)}",
                style = MaterialTheme.typography.labelMedium
            )
        },
        leadingContent = {
            val icon = getFileIcon(file.name)
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = onOpen) {
                    Icon(Icons.Default.OpenInNew, contentDescription = "Open")
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

fun getFileIcon(fileName: String): ImageVector {
    val extension = fileName.substringAfterLast('.', "").lowercase()
    return when (extension) {
        "jpg", "jpeg", "png", "webp", "gif" -> Icons.Default.Image
        "mp4", "mkv", "avi", "mov" -> Icons.Default.VideoFile
        "pdf" -> Icons.Default.PictureAsPdf
        "doc", "docx", "txt", "rtf" -> Icons.Default.Description
        "zip", "rar", "7z", "tar", "gz" -> Icons.Default.FolderZip
        "mp3", "wav", "m4a", "ogg" -> Icons.Default.AudioFile
        else -> Icons.Default.InsertDriveFile
    }
}

fun formatFileSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

fun formatDate(lastModified: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(lastModified))
}

fun openFile(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val extension = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString())
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open file with"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
