package com.kaimera.tablet.features.maps.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kaimera.tablet.features.maps.data.dao.MapsDao
import com.kaimera.tablet.features.maps.data.entities.PlaceCategory
import com.kaimera.tablet.features.maps.data.entities.SavedPlace
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(entities = [SavedPlace::class, PlaceCategory::class], version = 1, exportSchema = false)
abstract class MapsDatabase : RoomDatabase() {
    abstract fun mapsDao(): MapsDao
}

@Module
@InstallIn(SingletonComponent::class)
object MapsDatabaseModule {
    @Provides
    @Singleton
    fun provideMapsDatabase(@ApplicationContext context: Context): MapsDatabase {
        return Room.databaseBuilder(
            context,
            MapsDatabase::class.java,
            "kaimera_maps.db"
        ).build()
    }

    @Provides
    fun provideMapsDao(database: MapsDatabase): MapsDao {
        return database.mapsDao()
    }
}
