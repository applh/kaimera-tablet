package com.kaimera.tablet.features.camera

import androidx.lifecycle.ViewModel
import com.kaimera.tablet.core.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CameraViewModel @Inject constructor(
    val cameraManager: CameraManager,
    val userPreferences: UserPreferencesRepository
) : ViewModel()
