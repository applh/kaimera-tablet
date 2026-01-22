package com.kaimera.tablet.features.settings

import androidx.lifecycle.ViewModel
import com.kaimera.tablet.core.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    val userPreferences: UserPreferencesRepository
) : ViewModel()
