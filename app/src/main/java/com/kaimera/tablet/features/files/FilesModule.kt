package com.kaimera.tablet.features.files

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FilesModule {

    @Provides
    @Singleton
    fun provideFilesRepository(
        @ApplicationContext context: Context
    ): FilesRepository {
        return FilesRepositoryImpl(context)
    }
}
