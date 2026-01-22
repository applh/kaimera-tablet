package com.kaimera.tablet.features.calendar

import kotlinx.coroutines.delay
import javax.inject.Inject

interface CalendarRepository {
    suspend fun getData(): String
}

class CalendarRepositoryImpl @Inject constructor() : CalendarRepository {
    override suspend fun getData(): String {
        delay(1000) // Simulate network delay
        return "Hello from CalendarRepository!"
    }
}
