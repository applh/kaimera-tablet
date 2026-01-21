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
        val KEY_PHOTO_RESOLUTION_TIER = intPreferencesKey("photo_resolution_tier") // 0: HD, 1: FHD, 2: MAX
        val KEY_VIDEO_RESOLUTION_TIER = intPreferencesKey("video_resolution_tier") // 0: HD, 1: FHD, 2: 4K
        val KEY_VIDEO_FPS = intPreferencesKey("video_fps") // 30 or 60
        val KEY_JPEG_QUALITY = intPreferencesKey("jpeg_quality") // 1-100
        val KEY_CIRCLE_RADIUS_PERCENT = intPreferencesKey("circle_radius_percent") // 0-100
        val KEY_CAPTURE_MODE = intPreferencesKey("capture_mode") // 0: Latency, 1: Quality
        val KEY_IS_DEBUG_MODE = androidx.datastore.preferences.core.booleanPreferencesKey("is_debug_mode")
        val KEY_SCAN_QR_CODES = androidx.datastore.preferences.core.booleanPreferencesKey("scan_qr_codes")
        val KEY_AWB_MODE = intPreferencesKey("awb_mode") // CaptureRequest.CONTROL_AWB_MODE
        val KEY_TORCH_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("torch_enabled")
        val KEY_AI_SCENE_DETECTION = androidx.datastore.preferences.core.booleanPreferencesKey("ai_scene_detection")
        val KEY_TIMELAPSE_MODE = androidx.datastore.preferences.core.booleanPreferencesKey("timelapse_mode")
        val KEY_TIMELAPSE_INTERVAL = androidx.datastore.preferences.core.longPreferencesKey("timelapse_interval")
    }

    val gridRows: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_GRID_ROWS] ?: 2 }

    val gridCols: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_GRID_COLS] ?: 2 }

    val timerSeconds: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_TIMER_SECONDS] ?: 0 }

    val flashMode: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_FLASH_MODE] ?: 2 }

    val photoResolutionTier: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_PHOTO_RESOLUTION_TIER] ?: 1 }

    val videoResolutionTier: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_VIDEO_RESOLUTION_TIER] ?: 1 }

    val videoFps: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_VIDEO_FPS] ?: 30 }

    val jpegQuality: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_JPEG_QUALITY] ?: 95 }

    val circleRadiusPercent: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_CIRCLE_RADIUS_PERCENT] ?: 20 }

    val captureMode: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_CAPTURE_MODE] ?: 1 } // Default to 1 (Maximize Quality)

    val isDebugMode: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_IS_DEBUG_MODE] ?: false }

    val scanQrCodes: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_SCAN_QR_CODES] ?: false }

    val awbMode: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_AWB_MODE] ?: 1 } // Default to 1 (AUTO)

    val torchEnabled: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_TORCH_ENABLED] ?: false }

    val aiSceneDetection: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_AI_SCENE_DETECTION] ?: false }

    val timelapseMode: Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[KEY_TIMELAPSE_MODE] ?: false }

    val timelapseInterval: Flow<Long> = context.dataStore.data
        .map { preferences -> preferences[KEY_TIMELAPSE_INTERVAL] ?: 2000L } // Default 2s

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

    suspend fun setPhotoResolutionTier(tier: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_PHOTO_RESOLUTION_TIER] = tier
        }
    }

    suspend fun setVideoResolutionTier(tier: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_VIDEO_RESOLUTION_TIER] = tier
        }
    }

    suspend fun setVideoFps(fps: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_VIDEO_FPS] = fps
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

    suspend fun setDebugMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_DEBUG_MODE] = enabled
        }
    }

    suspend fun setScanQrCodes(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SCAN_QR_CODES] = enabled
        }
    }

    suspend fun setAwbMode(mode: Int) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AWB_MODE] = mode
        }
    }

    suspend fun setTorchEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TORCH_ENABLED] = enabled
        }
    }

    suspend fun setAiSceneDetection(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_AI_SCENE_DETECTION] = enabled
        }
    }

    suspend fun setTimelapseMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TIMELAPSE_MODE] = enabled
        }
    }

    suspend fun setTimelapseInterval(intervalMs: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TIMELAPSE_INTERVAL] = intervalMs
        }
    }
}
