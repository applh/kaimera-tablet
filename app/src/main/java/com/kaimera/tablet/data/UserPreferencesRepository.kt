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
        val KEY_FLASH_MODE = intPreferencesKey("flash_mode") // 0: OFF, 1: ON, 2: AUTO
    }

    val gridRows: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_GRID_ROWS] ?: 0 }

    val gridCols: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_GRID_COLS] ?: 0 }

    val timerSeconds: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_TIMER_SECONDS] ?: 0 }

    val flashMode: Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[KEY_FLASH_MODE] ?: 0 }

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
}
