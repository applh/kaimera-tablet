package com.kaimera.tablet.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "project_spaces")
data class ProjectSpace(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String, // Material Icon ID
    val color: Int // Hex color
)

@Entity(
    tableName = "projects",
    foreignKeys = [
        ForeignKey(
            entity = ProjectSpace::class,
            parentColumns = ["id"],
            childColumns = ["spaceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["spaceId"])]
)
data class Project(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val spaceId: Long,
    val name: String,
    val description: String?,
    val status: String = "Active", // Customizable
    val deadline: Long?,
    val progress: Int = 0 // 0-100
)

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey( // Self-referencing for subtasks
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"]), Index(value = ["parentId"])]
)
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val parentId: Long? = null,
    val title: String,
    val description: String?,
    val status: String = "Todo", // Customizable
    val priority: Priority = Priority.MEDIUM,
    val assignedToUserId: Long? = null,
    val dueDate: Long?,
    val estimatedDurationMinutes: Long?
)

enum class Priority {
    LOW, MEDIUM, HIGH, CRITICAL
}
