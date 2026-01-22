package com.kaimera.tablet.features.maps.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "place_categories")
data class PlaceCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String, // Material Icon name
    val color: Int // Hex color
)

@Entity(
    tableName = "saved_places",
    foreignKeys = [
        ForeignKey(
            entity = PlaceCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class SavedPlace(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String? = null,
    val categoryId: Long? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
