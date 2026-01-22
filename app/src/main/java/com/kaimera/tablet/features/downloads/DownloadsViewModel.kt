package com.kaimera.tablet.features.downloads

import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _files = MutableStateFlow<List<DownloadFile>>(emptyList())
    val files: StateFlow<List<DownloadFile>> = _files.asStateFlow()

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
        
        _files.value = fileList
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
}
