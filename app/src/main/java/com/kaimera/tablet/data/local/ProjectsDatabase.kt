package com.kaimera.tablet.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.kaimera.tablet.data.local.dao.ProjectDao
import com.kaimera.tablet.data.local.dao.TaskDao
import com.kaimera.tablet.data.local.entities.Project
import com.kaimera.tablet.data.local.entities.ProjectSpace
import com.kaimera.tablet.data.local.entities.ProjectTeam
import com.kaimera.tablet.data.local.entities.Task
import com.kaimera.tablet.data.local.entities.Team
import com.kaimera.tablet.data.local.entities.TeamMember
import com.kaimera.tablet.data.local.entities.User
import com.kaimera.tablet.data.local.entities.Priority
import com.kaimera.tablet.data.local.entities.Tag
import com.kaimera.tablet.data.local.entities.TaskTag
import com.kaimera.tablet.data.local.entities.MediaCollection
import com.kaimera.tablet.data.local.entities.MediaItem
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Date
import javax.inject.Singleton

@Database(
    entities = [
        ProjectSpace::class,
        Project::class,
        Task::class,
        User::class,
        Team::class,
        TeamMember::class,
        ProjectTeam::class,
        Tag::class,
        TaskTag::class,
        MediaCollection::class,
        MediaItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ProjectsDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun taskDao(): TaskDao
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromPriority(value: String?): Priority? {
        return value?.let { Priority.valueOf(it) }
    }

    @TypeConverter
    fun priorityToString(priority: Priority?): String? {
        return priority?.name
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideProjectsDatabase(@ApplicationContext context: Context): ProjectsDatabase {
        return Room.databaseBuilder(
            context,
            ProjectsDatabase::class.java,
            "kaimera_projects.db"
        ).build()
    }

    @Provides
    fun provideProjectDao(database: ProjectsDatabase): ProjectDao {
        return database.projectDao()
    }

    @Provides
    fun provideTaskDao(database: ProjectsDatabase): TaskDao {
        return database.taskDao()
    }
}
