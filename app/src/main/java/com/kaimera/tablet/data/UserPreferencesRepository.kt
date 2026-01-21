package com.kaimera.tablet.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    companion object {
        val KEY_GRID_ROWS = intPreferencesKey("grid_rows")
        val KEY_GRID_COLS = intPreferencesKey("grid_cols")
        val KEY_TIMER_SECONDS = intPreferencesKey("timer_seconds")
        val KEY_FLASH_MODE = intPreferencesKey("flash_mode")
        val KEY_RESOLUTION_TIER = intPreferencesKey("resolution_tier") // 0: HD, 1: FHD, 2: MAX
        val KEY_JPEG_QUALITY = intPreferencesKey("jpeg_quality") // 1-100
        val KEY_CIRCLE_RADIUS_PERCENT = intPreferencesKey("circle_radius_percent") // 0-100
        val KEY_CAPTURE_MODE = intPreferencesKey("capture_mode") // 0: Latency, 1: Quality
    }

    val gridRows: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_GRID_ROWS] ?: 2 }

    val gridCols: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_GRID_COLS] ?: 2 }

    val timerSeconds: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_TIMER_SECONDS] ?: 0 }

    val flashMode: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_FLASH_MODE] ?: 2 }

    val resolutionTier: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_RESOLUTION_TIER] ?: 1 }

    val jpegQuality: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_JPEG_QUALITY] ?: 95 }

    val circleRadiusPercent: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_CIRCLE_RADIUS_PERCENT] ?: 20 }

    val captureMode: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_CAPTURE_MODE] ?: 1 } // Default to 1 (Maximize Quality)

    suspend fun setGridRows(rows: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GRID_ROWS] = rows
        }
    }

    suspend fun setGridCols(cols: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_GRID_COLS] = cols
        }
    }

    suspend fun setTimerSeconds(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TIMER_SECONDS] = seconds
        }
    }

    suspend fun setFlashMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_FLASH_MODE] = mode
        }
    }

    suspend fun setResolutionTier(tier: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_RESOLUTION_TIER] = tier
        }
    }

    suspend fun setJpegQuality(quality: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_JPEG_QUALITY] = quality
        }
    }

    suspend fun setCircleRadiusPercent(percent: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CIRCLE_RADIUS_PERCENT] = percent
        }
    }

    suspend fun setCaptureMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_CAPTURE_MODE] = mode
        }
    }
}
