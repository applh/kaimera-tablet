package com.kaimera.tablet.files

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

@Composable
fun FilesScreen(
    viewModel: FilesViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(FilesViewModel::class.java)) {
                    return FilesViewModel(FilesRepositoryImpl()) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = uiState) {
            is FilesUiState.Loading -> {
                CircularProgressIndicator()
            }
            is FilesUiState.Success -> {
                Column {
                    Text(text = "Data Loaded:")
                    Text(text = state.data)
                }
            }
            is FilesUiState.Error -> {
                Text(text = "Error: \${state.message}")
            }
        }
    }
}
