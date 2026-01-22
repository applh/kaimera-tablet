package com.kaimera.tablet.features.files

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val repository: FilesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FilesUiState>(FilesUiState.Loading)
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    private val _actionEffect = MutableStateFlow<FilesActionEffect?>(null)
    val actionEffect: StateFlow<FilesActionEffect?> = _actionEffect.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = FilesUiState.Loading
            try {
                val data = repository.getMedia()
                _uiState.value = FilesUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = FilesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    fun deleteFile(file: MediaFile) {
        viewModelScope.launch {
            val intentSender = repository.deleteFile(file.uri)
            if (intentSender != null) {
                _actionEffect.value = FilesActionEffect.RequestDeletePermission(intentSender)
            } else {
                // Determine success/fail, simplistically reload
                loadData()
            }
        }
    }

    fun renameFile(file: MediaFile, newName: String) {
        viewModelScope.launch {
            val intentSender = repository.renameFile(file.uri, newName)
            if (intentSender != null) {
                _actionEffect.value = FilesActionEffect.RequestRenamePermission(intentSender)
            } else {
                loadData()
            }
        }
    }
    
    fun clearEffect() {
        _actionEffect.value = null
    }
}

sealed class FilesUiState {
    object Loading : FilesUiState()
    data class Success(val media: List<MediaFile>) : FilesUiState()
    data class Error(val message: String) : FilesUiState()
}

sealed class FilesActionEffect {
    data class RequestDeletePermission(val intentSender: android.content.IntentSender) : FilesActionEffect()
    data class RequestRenamePermission(val intentSender: android.content.IntentSender) : FilesActionEffect()
}
