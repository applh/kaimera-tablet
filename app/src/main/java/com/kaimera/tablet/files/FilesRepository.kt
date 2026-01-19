package com.kaimera.tablet.files

import kotlinx.coroutines.delay

interface FilesRepository {
    suspend fun getData(): String
}

class FilesRepositoryImpl : FilesRepository {
    override suspend fun getData(): String {
        delay(1000) // Simulate network delay
        return "Hello from FilesRepository!"
    }
}
