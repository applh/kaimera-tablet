package com.kaimera.tablet.features.downloads

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import android.webkit.URLUtil
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class DownloadFile(
    val file: File,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean
)

data class StorageInfo(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercent: Float
)

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    enum class DownloadsCategory(val id: String, val label: String) {
        ALL("all", "All Files"),
        RECENT("recent", "Recent"),
        IMAGES("images", "Images"),
        VIDEOS("videos", "Videos"),
        DOCUMENTS("documents", "Documents"),
        OTHERS("others", "Others")
    }

    private val _rawFiles = MutableStateFlow<List<DownloadFile>>(emptyList())
    
    private val _selectedCategory = MutableStateFlow(DownloadsCategory.ALL)
    val selectedCategory: StateFlow<DownloadsCategory> = _selectedCategory.asStateFlow()

    val files: StateFlow<List<DownloadFile>> = combine(_rawFiles, _selectedCategory) { files, category ->
        filterFiles(files, category)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _storageInfo = MutableStateFlow<StorageInfo?>(null)
    val storageInfo: StateFlow<StorageInfo?> = _storageInfo.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadFiles()
            loadStorageInfo()
            _isRefreshing.value = false
        }
    }

    fun onCategorySelected(category: DownloadsCategory) {
        _selectedCategory.value = category
    }

    private fun loadFiles() {
        val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val fileList = dir.listFiles()?.map {
            DownloadFile(
                file = it,
                name = it.name,
                size = it.length(),
                lastModified = it.lastModified(),
                isDirectory = it.isDirectory
            )
        }?.sortedByDescending { it.lastModified } ?: emptyList()
        
        _rawFiles.value = fileList
    }

    private fun filterFiles(files: List<DownloadFile>, category: DownloadsCategory): List<DownloadFile> {
        return when (category) {
            DownloadsCategory.ALL -> files
            DownloadsCategory.RECENT -> {
                val oneWeekAgo = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
                files.filter { it.lastModified >= oneWeekAgo }
            }
            DownloadsCategory.IMAGES -> files.filter { 
                it.name.substringAfterLast('.', "").lowercase() in setOf("jpg", "jpeg", "png", "webp", "gif") 
            }
            DownloadsCategory.VIDEOS -> files.filter { 
                it.name.substringAfterLast('.', "").lowercase() in setOf("mp4", "mkv", "avi", "mov", "webm") 
            }
            DownloadsCategory.DOCUMENTS -> files.filter { 
                it.name.substringAfterLast('.', "").lowercase() in setOf("pdf", "doc", "docx", "txt", "rtf", "xls", "xlsx", "ppt", "pptx") 
            }
            DownloadsCategory.OTHERS -> files.filter {
                val ext = it.name.substringAfterLast('.', "").lowercase()
                ext !in setOf("jpg", "jpeg", "png", "webp", "gif", "mp4", "mkv", "avi", "mov", "webm", "pdf", "doc", "docx", "txt", "rtf", "xls", "xlsx", "ppt", "pptx")
            }
        }
    }

    private fun loadStorageInfo() {
        val path = Environment.getExternalStorageDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong

        val totalBytes = totalBlocks * blockSize
        val availableBytes = availableBlocks * blockSize
        val usedBytes = totalBytes - availableBytes
        val usedPercent = if (totalBytes > 0) usedBytes.toFloat() / totalBytes.toFloat() else 0f

        _storageInfo.value = StorageInfo(
            totalBytes = totalBytes,
            availableBytes = availableBytes,
            usedBytes = usedBytes,
            usedPercent = usedPercent
        )
    }

    fun deleteFile(downloadFile: DownloadFile) {
        viewModelScope.launch {
            if (downloadFile.file.exists()) {
                downloadFile.file.delete()
                refresh()
            }
        }
    }

    fun downloadUrl(url: String) {
        try {
            val request = DownloadManager.Request(Uri.parse(url)).apply {
                val fileName = URLUtil.guessFileName(url, null, null)
                setTitle(fileName)
                setDescription("Downloading manually added file...")
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
}
