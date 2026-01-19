package com.kaimera.tablet.files

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FilesViewModel(
    private val repository: FilesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<FilesUiState>(FilesUiState.Loading)
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = FilesUiState.Loading
            try {
                val data = repository.getData()
                _uiState.value = FilesUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = FilesUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class FilesUiState {
    object Loading : FilesUiState()
    data class Success(val data: String) : FilesUiState()
    data class Error(val message: String) : FilesUiState()
}
