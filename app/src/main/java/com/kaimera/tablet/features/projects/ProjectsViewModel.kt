package com.kaimera.tablet.features.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kaimera.tablet.core.ui.components.TreeNode
import com.kaimera.tablet.data.local.entities.Project
import com.kaimera.tablet.data.local.entities.Task
import com.kaimera.tablet.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.HomeWork

@HiltViewModel
class ProjectsViewModel @Inject constructor(
    private val repository: ProjectRepository
) : ViewModel() {

    private val _selectedNodeId = MutableStateFlow<String?>(null)
    val selectedNodeId: StateFlow<String?> = _selectedNodeId.asStateFlow()

    private val _filter = MutableStateFlow<String?>(null)
    private val _sort = MutableStateFlow<String>("Date")
    val filter: StateFlow<String?> = _filter.asStateFlow()
    
    // View Mode: "List" or "Board"
    private val _viewMode = MutableStateFlow("List")
    val viewMode: StateFlow<String> = _viewMode.asStateFlow()

    val treeNodes: StateFlow<List<TreeNode>> = combine(
        repository.allSpaces,
        repository.getAllProjects()
    ) { spaces, projects ->
        spaces.map { space ->
            TreeNode(
                id = "space_${space.id}",
                label = space.name,
                icon = Icons.Default.HomeWork,
                children = projects.filter { it.spaceId == space.id }.map { project ->
                    TreeNode(
                        id = "project_${project.id}",
                        label = project.name,
                        icon = Icons.Default.Assignment
                    )
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Dashboard Data Aggregation
    val dashboardData: StateFlow<DashboardUiState> = repository.getAllProjects().flatMapLatest { projects ->
        // In a real app, we would query tasks for all projects or recent activities here
        // For MVP, simplistic dummy data or empty state if no projects
        flowOf(DashboardUiState(
            activeProjectsCount = projects.size,
            recentTasks = emptyList(), // TODO: Fetch from DB
            overdueTasksCount = 0 // TODO: Query DB
        ))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    // Filtered Tasks for Selected Project
    val currentTasks: StateFlow<List<Task>> = combine(
        _selectedNodeId,
        _filter,
        _sort
    ) { nodeId, filterVal, sortVal ->
        Triple(nodeId, filterVal, sortVal)
    }.flatMapLatest { (nodeId, filterVal, sortVal) ->
        if (nodeId != null && nodeId.startsWith("project_")) {
            val projectId = nodeId.removePrefix("project_").toLongOrNull()
            if (projectId != null) {
                repository.getTasksForProject(projectId).map { tasks ->
                    var filtered = tasks
                    if (filterVal != null) {
                         // Simple filter mapping
                         filtered = when(filterVal) {
                             "High" -> tasks.filter { it.priority.name == "HIGH" || it.priority.name == "CRITICAL" } // Assuming Priority Enum string
                             "Doing" -> tasks.filter { it.status == "Doing" }
                             else -> tasks
                         }
                    }
                    
                    // Simple Sort
                    when(sortVal) {
                        "Priority" -> filtered.sortedByDescending { it.priority } 
                        else -> filtered.sortedBy { it.dueDate ?: 0L }
                    }
                }
            } else {
                flowOf(emptyList())
            }
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    fun selectNode(nodeId: String) {
        _selectedNodeId.value = nodeId
    }
    
    fun setFilter(filter: String?) {
        _filter.value = filter
    }
    
    fun setSort(sort: String) {
        _sort.value = sort
    }
    
    fun setViewMode(mode: String) {
        _viewMode.value = mode
    }

    // --- CRUD Operations ---

    fun createSpace(name: String) {
        viewModelScope.launch {
            repository.createSpace(name, "home_work", 0xFF000000.toInt())
        }
    }

    fun createProject(spaceId: Long, name: String, description: String?) {
        viewModelScope.launch {
            repository.createProject(
                Project(
                    spaceId = spaceId,
                    name = name,
                    description = description,
                    deadline = null
                )
            )
        }
    }

    fun createTask(projectId: Long, title: String, assigneeId: Long? = null, deadline: Long? = null) {
        viewModelScope.launch {
            repository.createTask(
                Task(
                    projectId = projectId,
                    title = title,
                    description = "",
                    dueDate = deadline,
                    assignedToUserId = assigneeId,
                    estimatedDurationMinutes = null
                )
            )
        }
    }
    
    fun updateTaskStatus(task: Task, status: String) {
        viewModelScope.launch {
            repository.updateTask(task.copy(status = status))
        }
    }
}

data class DashboardUiState(
    val activeProjectsCount: Int = 0,
    val recentTasks: List<Task> = emptyList(),
    val overdueTasksCount: Int = 0
)

// Mock Data for Phase D
data class MockUser(val id: Long, val name: String, val initials: String)
val MOCK_USERS = listOf(
    MockUser(1, "Alice", "A"),
    MockUser(2, "Bob", "B"),
    MockUser(3, "Charlie", "C")
)
