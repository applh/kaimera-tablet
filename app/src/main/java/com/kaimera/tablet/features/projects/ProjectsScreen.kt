package com.kaimera.tablet.features.projects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.kaimera.tablet.core.ui.components.TreeNode
import com.kaimera.tablet.core.ui.components.NavDrawerTreePanel
import kotlinx.coroutines.launch
import com.kaimera.tablet.data.local.entities.Task

import com.kaimera.tablet.features.projects.components.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onBack: () -> Unit = {},
    viewModel: ProjectsViewModel = hiltViewModel()
) {
    val treeNodes by viewModel.treeNodes.collectAsState()
    val selectedNodeId by viewModel.selectedNodeId.collectAsState()
    val tasks by viewModel.currentTasks.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val dashboardData by viewModel.dashboardData.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var showAddSpaceDialog by remember { mutableStateOf(false) }

    var showAddProjectDialog by remember { mutableStateOf(false) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val isProjectSelected = selectedNodeId?.startsWith("project_") == true
    val isSpaceSelected = selectedNodeId?.startsWith("space_") == true

    if (showAddSpaceDialog) {
        AddSpaceDialog(
            onDismiss = { showAddSpaceDialog = false },
            onConfirm = { name -> viewModel.createSpace(name); showAddSpaceDialog = false }
        )
    }

    if (showAddProjectDialog && isSpaceSelected) {
        AddProjectDialog(
            onDismiss = { showAddProjectDialog = false },
            onConfirm = { name, desc ->
                val spaceId = selectedNodeId?.removePrefix("space_")?.toLongOrNull()
                if (spaceId != null) viewModel.createProject(spaceId, name, desc)
                showAddProjectDialog = false
            }
        )
    }

    if (showAddTaskDialog && isProjectSelected) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, assigneeName, deadline ->
                val projectId = selectedNodeId?.removePrefix("project_")?.toLongOrNull()
                if (projectId != null) {
                    val assigneeId = MOCK_USERS.find { it.name == assigneeName }?.id
                    viewModel.createTask(projectId, title, assigneeId, deadline)
                }
                showAddTaskDialog = false
            }
        )
    }

    NavDrawerTreePanel(
        drawerState = drawerState,
        title = "Projects",
        onHomeClick = onBack,
        nodes = treeNodes,
        selectedNodeId = selectedNodeId,

        onNodeSelected = { viewModel.selectNode(it.id) }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Projects") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },

                    actions = {
                    if (isProjectSelected) {
                         IconButton(onClick = { viewModel.setViewMode(if (viewMode == "List") "Board" else "List") }) {
                            Icon(
                                if (viewMode == "List") Icons.Default.ViewKanban else Icons.Default.List,
                                contentDescription = "Toggle View"
                            )
                        }
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("New Space") },
                                onClick = { showMenu = false; showAddSpaceDialog = true },
                                leadingIcon = { Icon(Icons.Default.HomeWork, null) }
                            )
                            if (isSpaceSelected) {
                                DropdownMenuItem(
                                    text = { Text("New Project") },
                                    onClick = { showMenu = false; showAddProjectDialog = true },
                                    leadingIcon = { Icon(Icons.Default.Assignment, null) }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isProjectSelected) {
                FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Task")
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {

                if (isProjectSelected) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        
                        TaskFilters(
                            onFilterChange = { viewModel.setFilter(it) },
                            onSortChange = { viewModel.setSort(it) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        if (tasks.isEmpty()) {
                            EmptyProjectState()
                        } else if (viewMode == "Board") {
                            KanbanBoard(
                                tasks = tasks,
                                onStatusChange = { task, status -> viewModel.updateTaskStatus(task, status) }
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(tasks) { task ->
                                    TaskCard(task, onStatusChange = { isDone ->
                                        viewModel.updateTaskStatus(task, if(isDone) "Done" else "Todo")
                                    })
                                }
                            }
                        }
                    }
                } else if (isSpaceSelected) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                         Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Assignment, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.3f))
                            Spacer(Modifier.height(16.dp))
                            Text("Select a Project to view tasks", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    DashboardView(dashboardData)
                }
            }
        }
        }
    }
}


@Composable
fun EmptyProjectState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.AddTask, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha=0.5f))
        Spacer(Modifier.height(16.dp))
        Text("No tasks yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Add a task to get started", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun DashboardView(data: DashboardUiState) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Dashboard, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Projects Dashboard", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            DashboardCard("Active Projects", data.activeProjectsCount.toString(), Icons.Default.Folder)
            DashboardCard("Overdue Tasks", data.overdueTasksCount.toString(), Icons.Default.Warning)
        }
    }
}

@Composable
fun DashboardCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier.size(160.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TaskCard(task: Task, onStatusChange: (Boolean) -> Unit) {
    val isDone = task.status == "Done"
    val statusColor = if (isDone) Color(0xFF4CAF50) else Color(0xFF2196F3)
    val assignee = MOCK_USERS.find { it.id == task.assignedToUserId }
    val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isDone,
                onCheckedChange = onStatusChange
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                     Text(
                        text = task.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (task.dueDate != null) {
                         Row(verticalAlignment = Alignment.CenterVertically) {
                             Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                             Spacer(Modifier.width(4.dp))
                             Text(formatter.format(Date(task.dueDate)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                         }
                    }
                    
                    // Mock Attachment Indicator
                    Icon(Icons.Default.AttachFile, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f))
                }
            }
            
            if (assignee != null) {
                Box(
                    modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(assignee.initials, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
