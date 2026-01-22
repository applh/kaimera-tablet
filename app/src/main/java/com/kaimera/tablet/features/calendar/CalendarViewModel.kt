package com.kaimera.tablet.features.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CalendarUiState>(CalendarUiState.Loading)
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = CalendarUiState.Loading
            try {
                val data = repository.getData()
                _uiState.value = CalendarUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = CalendarUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class CalendarUiState {
    object Loading : CalendarUiState()
    data class Success(val data: String) : CalendarUiState()
    data class Error(val message: String) : CalendarUiState()
}
