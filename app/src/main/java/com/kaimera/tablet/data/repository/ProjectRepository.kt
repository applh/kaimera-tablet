package com.kaimera.tablet.data.repository

import com.kaimera.tablet.data.local.dao.ProjectDao
import com.kaimera.tablet.data.local.dao.TaskDao
import com.kaimera.tablet.data.local.entities.Project
import com.kaimera.tablet.data.local.entities.ProjectSpace
import com.kaimera.tablet.data.local.entities.ProjectWithTasks
import com.kaimera.tablet.data.local.entities.Task
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
    private val taskDao: TaskDao
) {
    // --- Spaces ---
    val allSpaces: Flow<List<ProjectSpace>> = projectDao.getAllSpaces()

    suspend fun createSpace(name: String, icon: String, color: Int) {
        projectDao.insertSpace(ProjectSpace(name = name, icon = icon, color = color))
    }

    // --- Projects ---
    fun getProjectsInSpace(spaceId: Long): Flow<List<Project>> =
        projectDao.getProjectsBySpace(spaceId)

    fun getAllProjects(): Flow<List<Project>> = projectDao.getAllProjects()

    fun getProjectWithTasks(projectId: Long): Flow<ProjectWithTasks> =
        projectDao.getProjectWithTasks(projectId)

    suspend fun createProject(project: Project) {
        projectDao.insertProject(project)
    }

    suspend fun updateProject(project: Project) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(project: Project) {
        projectDao.deleteProject(project)
    }

    // --- Tasks ---
    fun getTasksForProject(projectId: Long): Flow<List<Task>> =
        taskDao.getTasksForProject(projectId)
    
    suspend fun createTask(task: Task) {
        taskDao.insertTask(task)
    }
    
    suspend fun updateTask(task: Task) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }
}
