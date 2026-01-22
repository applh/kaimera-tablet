package com.kaimera.tablet.core.di

import com.kaimera.tablet.features.calendar.CalendarRepository
import com.kaimera.tablet.features.calendar.CalendarRepositoryImpl
import com.kaimera.tablet.features.projects.ProjectsRepository
import com.kaimera.tablet.features.projects.ProjectsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(
        impl: CalendarRepositoryImpl
    ): CalendarRepository

    @Binds
    @Singleton
    abstract fun bindProjectsRepository(
        impl: ProjectsRepositoryImpl
    ): ProjectsRepository
}
