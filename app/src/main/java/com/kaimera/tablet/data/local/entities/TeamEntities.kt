package com.kaimera.tablet.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val avatarUri: String?,
    val role: String // "Admin", "Editor", "Viewer"
)

@Entity(tableName = "teams")
data class Team(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String?
)

@Entity(
    tableName = "team_members",
    primaryKeys = ["userId", "teamId"],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Team::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"]), Index(value = ["teamId"])]
)
data class TeamMember(
    val userId: Long,
    val teamId: Long,
    val role: String // "Lead", "Member"
)

@Entity(
    tableName = "project_teams",
    primaryKeys = ["projectId", "teamId"],
    foreignKeys = [
        ForeignKey(
            entity = com.kaimera.tablet.data.local.entities.Project::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Team::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["projectId"]), Index(value = ["teamId"])]
)
data class ProjectTeam(
    val projectId: Long,
    val teamId: Long,
    val permissions: String, // "Read", "Write", "Admin"
    val startDate: Long?,
    val endDate: Long?
)
