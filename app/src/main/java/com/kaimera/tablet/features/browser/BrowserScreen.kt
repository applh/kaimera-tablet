package com.kaimera.tablet.features.browser

import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.URLUtil
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaimera.tablet.core.ui.components.NavDrawerTreePanel
import com.kaimera.tablet.core.ui.components.TreeNode
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    onBack: () -> Unit,
    viewModel: BrowserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentUrl by viewModel.url.collectAsStateWithLifecycle()
    val loadProgress by viewModel.loadProgress.collectAsStateWithLifecycle()
    val canGoBack by viewModel.canGoBack.collectAsStateWithLifecycle()
    val canGoForward by viewModel.canGoForward.collectAsStateWithLifecycle()

    var textFieldValue by remember { mutableStateOf(currentUrl) }
    val focusManager = LocalFocusManager.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val treeNodes = remember {
        listOf(
            TreeNode("history", "History", Icons.Default.History),
            TreeNode("bookmarks", "Bookmarks", Icons.Default.Bookmark),
            TreeNode("downloads", "Downloads", Icons.Default.Download)
        )
    }
    var selectedNodeId by remember { mutableStateOf<String?>(null) }


    // Context Menu State
    var showContextMenu by remember { mutableStateOf(false) }
    var contextMenuUrl by remember { mutableStateOf("") }

    // Sync textFieldValue when currentUrl changes from external source
    LaunchedEffect(currentUrl) {
        if (textFieldValue != currentUrl) {
            textFieldValue = currentUrl
        }
    }

    NavDrawerTreePanel(
        drawerState = drawerState,
        title = "Browser",
        onHomeClick = onBack,
        nodes = treeNodes,
        selectedNodeId = selectedNodeId,
        onNodeSelected = { 
            selectedNodeId = it.id 
            // Handle navigation or filter if needed
        }
    ) {
        Scaffold(

        topBar = {
            Column {
                TopAppBar(
                    title = {
                        TextField(
                            value = textFieldValue,
                            onValueChange = { textFieldValue = it },
                            modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                            placeholder = { Text("Search or enter URL") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                            trailingIcon = {
                                if (textFieldValue.isNotEmpty()) {
                                    IconButton(onClick = { textFieldValue = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(
                                onGo = {
                                    viewModel.updateUrl(textFieldValue)
                                    focusManager.clearFocus()
                                }
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },

                    actions = {
                        IconButton(onClick = { webViewRef?.reload() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reload")
                        }
                    }
                )
                if (loadProgress < 100) {
                    LinearProgressIndicator(
                        progress = { loadProgress / 100f },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
            }
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconButton(
                        onClick = { webViewRef?.goBack() },
                        enabled = canGoBack
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    IconButton(
                        onClick = { webViewRef?.goForward() },
                        enabled = canGoForward
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward")
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = { viewModel.updateUrl("https://www.google.com") }) {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                url?.let { 
                                    viewModel.updateNavState(canGoBack(), canGoForward())
                                    if (it != currentUrl) {
                                        textFieldValue = it
                                    }
                                }
                            }
                        }
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                viewModel.setProgress(newProgress)
                            }
                        }
                        setDownloadListener { url, _, contentDisposition, mimetype, _ ->
                            triggerDownload(context, url, contentDisposition, mimetype)
                        }

                        // Long Click Listener for Context Menu
                        setOnLongClickListener {
                            val result = hitTestResult
                            when (result.type) {
                                WebView.HitTestResult.SRC_ANCHOR_TYPE,
                                WebView.HitTestResult.IMAGE_TYPE,
                                WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> {
                                    contextMenuUrl = result.extra ?: ""
                                    if (contextMenuUrl.isNotEmpty()) {
                                        showContextMenu = true
                                    }
                                    true
                                }
                                WebView.HitTestResult.UNKNOWN_TYPE -> {
                                    // Potential video or other media not caught by standard types
                                    // HitTestResult doesn't have a VIDEO_TYPE, but extra might contain URL
                                    contextMenuUrl = result.extra ?: ""
                                    if (contextMenuUrl.isNotEmpty() && isVideoUrl(contextMenuUrl)) {
                                        showContextMenu = true
                                        true
                                    } else {
                                        false
                                    }
                                }
                                else -> false
                            }
                        }

                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        loadUrl(currentUrl)
                        webViewRef = this
                    }
                },
                update = { webView ->
                    if (webView.url != currentUrl) {
                        webView.loadUrl(currentUrl)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Custom Context Menu Overlay
            if (showContextMenu) {
                // Centered for now as touch coordinates are tricky from AndroidView factory
                AlertDialog(
                    onDismissRequest = { showContextMenu = false },
                    title = { 
                        Text(
                            text = contextMenuUrl,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    },
                    text = {
                        Column {
                            ListItem(
                                headlineContent = { Text("Open Link") },
                                modifier = Modifier.clickable {
                                    viewModel.updateUrl(contextMenuUrl)
                                    showContextMenu = false
                                },
                                leadingContent = { Icon(Icons.Default.OpenInNew, contentDescription = null) }
                            )
                            ListItem(
                                headlineContent = { Text("Copy URL") },
                                modifier = Modifier.clickable {
                                    copyToClipboard(context, contextMenuUrl)
                                    showContextMenu = false
                                },
                                leadingContent = { Icon(Icons.Default.ContentCopy, contentDescription = null) }
                            )
                            ListItem(
                                headlineContent = { Text("Download Link") },
                                modifier = Modifier.clickable {
                                    triggerDownload(context, contextMenuUrl, null, null)
                                    showContextMenu = false
                                },
                                leadingContent = { Icon(Icons.Default.FileDownload, contentDescription = null) }
                            )
                        }
                    },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showContextMenu = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
        }
    }
}


private fun isVideoUrl(url: String): Boolean {
    val videoExtensions = listOf(".mp4", ".mkv", ".webm", ".avi", ".mov")
    return videoExtensions.any { url.lowercase().endsWith(it) || url.lowercase().contains("$it?") }
}

private fun triggerDownload(context: Context, url: String, contentDisposition: String?, mimetype: String?) {
    try {
        val request = DownloadManager.Request(Uri.parse(url)).apply {
            setMimeType(mimetype)
            val fileName = URLUtil.guessFileName(url, contentDisposition, mimetype)
            setTitle(fileName)
            setDescription("Downloading file...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setAllowedOverMetered(true)
            setAllowedOverRoaming(true)
        }
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        dm.enqueue(request)
        Toast.makeText(context, "Download started...", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("URL", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
}
