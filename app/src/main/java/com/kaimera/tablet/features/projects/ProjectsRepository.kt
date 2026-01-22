package com.kaimera.tablet.features.projects

import kotlinx.coroutines.delay
import javax.inject.Inject

interface ProjectsRepository {
    suspend fun getData(): String
}

class ProjectsRepositoryImpl @Inject constructor() : ProjectsRepository {
    override suspend fun getData(): String {
        delay(1000) // Simulate network delay
        return "Hello from ProjectsRepository!"
    }
}
