package com.kaimera.tablet.data.local.entities

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

// 1. Project with its Tasks
data class ProjectWithTasks(
    @Embedded val project: Project,
    @Relation(
        parentColumn = "id",
        entityColumn = "projectId"
    )
    val tasks: List<Task>
)

// 2. Project with its assigned Teams
data class ProjectWithTeams(
    @Embedded val project: Project,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProjectTeam::class,
            parentColumn = "projectId",
            entityColumn = "teamId"
        )
    )
    val teams: List<Team>
)

// 3. Complete Project View (Space + Project + Teams)
data class ProjectComposite(
    @Embedded val project: Project,
    
    @Relation(
        parentColumn = "spaceId",
        entityColumn = "id"
    )
    val space: ProjectSpace,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = ProjectTeam::class,
            parentColumn = "projectId",
            entityColumn = "teamId"
        )
    )
    val teams: List<Team>
)
