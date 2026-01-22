package com.kaimera.tablet.features.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: ProjectsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProjectsUiState>(ProjectsUiState.Loading)
    val uiState: StateFlow<ProjectsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = ProjectsUiState.Loading
            try {
                val data = repository.getData()
                _uiState.value = ProjectsUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = ProjectsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class ProjectsUiState {
    object Loading : ProjectsUiState()
    data class Success(val data: String) : ProjectsUiState()
    data class Error(val message: String) : ProjectsUiState()
}
