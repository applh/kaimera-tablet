package com.kaimera.tablet.features.maps.data.dao

import androidx.room.*
import com.kaimera.tablet.features.maps.data.entities.PlaceCategory
import com.kaimera.tablet.features.maps.data.entities.SavedPlace
import kotlinx.coroutines.flow.Flow

@Dao
interface MapsDao {
    // Places
    @Query("SELECT * FROM saved_places ORDER BY timestamp DESC")
    fun getAllPlaces(): Flow<List<SavedPlace>>

    @Query("SELECT * FROM saved_places WHERE categoryId = :categoryId")
    fun getPlacesByCategory(categoryId: Long): Flow<List<SavedPlace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: SavedPlace): Long

    @Delete
    suspend fun deletePlace(place: SavedPlace)

    // Categories
    @Query("SELECT * FROM place_categories")
    fun getAllCategories(): Flow<List<PlaceCategory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: PlaceCategory): Long

    @Delete
    suspend fun deleteCategory(category: PlaceCategory)
}
