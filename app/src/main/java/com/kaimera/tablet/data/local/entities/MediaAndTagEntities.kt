package com.kaimera.tablet.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "tags")
data class Tag(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val color: Int,
    val scope: String // "GLOBAL", "PROJECT"
)

@Entity(
    tableName = "task_tags",
    primaryKeys = ["taskId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = Task::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Tag::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"]), Index(value = ["tagId"])]
)
data class TaskTag(
    val taskId: Long,
    val tagId: Long
)

@Entity(
    tableName = "media_collections",
    foreignKeys = [
        ForeignKey(
            entity = Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"])]
)
data class MediaCollection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val description: String?
)

@Entity(
    tableName = "media_items",
    foreignKeys = [
        ForeignKey(
            entity = MediaCollection::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["collectionId"])]
)
data class MediaItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val collectionId: Long,
    val uri: String,
    val type: String, // "IMAGE", "VIDEO", "DOC"
    val caption: String?
)
