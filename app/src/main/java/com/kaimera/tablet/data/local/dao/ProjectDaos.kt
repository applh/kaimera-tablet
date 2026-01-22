package com.kaimera.tablet.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.kaimera.tablet.data.local.entities.Project
import com.kaimera.tablet.data.local.entities.ProjectSpace
import com.kaimera.tablet.data.local.entities.ProjectWithTasks
import com.kaimera.tablet.data.local.entities.Task
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    // --- Spaces ---
    @Query("SELECT * FROM project_spaces")
    fun getAllSpaces(): Flow<List<ProjectSpace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpace(space: ProjectSpace): Long

    // --- Projects ---
    @Query("SELECT * FROM projects WHERE spaceId = :spaceId")
    fun getProjectsBySpace(spaceId: Long): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :projectId")
    suspend fun getProjectById(projectId: Long): Project?

    @Transaction // Ensure atomic fetch for relations
    @Query("SELECT * FROM projects WHERE id = :projectId")
    fun getProjectWithTasks(projectId: Long): Flow<ProjectWithTasks>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: Project): Long

    @Update
    suspend fun updateProject(project: Project)

    @Delete
    suspend fun deleteProject(project: Project)
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE projectId = :projectId")
    fun getTasksForProject(projectId: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE assignedToUserId = :userId")
    fun getTasksForUser(userId: Long): Flow<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)
}
