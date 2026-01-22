package com.kaimera.tablet.features.maps.data

import com.kaimera.tablet.features.maps.data.dao.MapsDao
import com.kaimera.tablet.features.maps.data.entities.PlaceCategory
import com.kaimera.tablet.features.maps.data.entities.SavedPlace
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MapsRepository @Inject constructor(
    private val mapsDao: MapsDao
) {
    val allPlaces: Flow<List<SavedPlace>> = mapsDao.getAllPlaces()
    val allCategories: Flow<List<PlaceCategory>> = mapsDao.getAllCategories()

    fun getPlacesByCategory(categoryId: Long): Flow<List<SavedPlace>> = 
        mapsDao.getPlacesByCategory(categoryId)

    suspend fun savePlace(place: SavedPlace) = mapsDao.insertPlace(place)
    
    suspend fun deletePlace(place: SavedPlace) = mapsDao.deletePlace(place)

    suspend fun saveCategory(category: PlaceCategory) = mapsDao.insertCategory(category)
}
