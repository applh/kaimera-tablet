package com.kaimera.tablet.features.projects

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.kaimera.tablet.core.ui.components.TreePanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    onBack: () -> Unit = {},
    viewModel: ProjectsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val treeNodes = remember {
        listOf(
            TreeNode("spaces", "Spaces", Icons.Default.HomeWork, children = listOf(
                TreeNode("eng", "Engineering", Icons.Default.Folder, children = listOf(
                    TreeNode("kaimera", "Kaimera Tablet", Icons.Default.Assignment),
                    TreeNode("backend", "API Service", Icons.Default.Assignment)
                )),
                TreeNode("design", "Design", Icons.Default.Folder, children = listOf(
                    TreeNode("uiux", "UI/UX Revamp", Icons.Default.Assignment)
                ))
            )),
            TreeNode("archive", "Archive", Icons.Default.Inventory)
        )
    }
    var selectedNodeId by remember { mutableStateOf("kaimera") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projects") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { /* Add Task */ }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Project")
                    }
                }
            )
        }
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TreePanel(
                nodes = treeNodes,
                selectedNodeId = selectedNodeId,
                onNodeSelected = { selectedNodeId = it.id },
                modifier = Modifier.width(260.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(24.dp)
            ) {
                when (val state = uiState) {
                    is ProjectsUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is ProjectsUiState.Success -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            ProjectHeader(selectedNodeId.replaceFirstChar { it.uppercase() })
                            
                            Spacer(modifier = Modifier.height(32.dp))
                            
                            Text(
                                text = "Tasks",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(listOf(
                                    TaskItem("Implement Tree Navigation", "High", 0.8f, "In-Progress"),
                                    TaskItem("Enhance Dashboard UI", "Medium", 0.4f, "In-Progress"),
                                    TaskItem("Fix Dependency Injection", "High", 1.0f, "Done"),
                                    TaskItem("Add Calendar API", "Low", 0.0f, "To-Do")
                                )) { task ->
                                    TaskCard(task)
                                }
                            }
                        }
                    }
                    is ProjectsUiState.Error -> {
                        Text(text = "Error: \${state.message}", modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

data class TaskItem(val title: String, val priority: String, val progress: Float, val status: String)

@Composable
fun ProjectHeader(name: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Active Project", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(text = name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "72%", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = 0.72f,
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun TaskCard(task: TaskItem) {
    val statusColor = when (task.status) {
        "Done" -> Color(0xFF4CAF50)
        "In-Progress" -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

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
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(statusColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(text = "Priority: \${task.priority}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = task.status, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                if (task.progress > 0f) {
                    Text(text = "\${(task.progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
