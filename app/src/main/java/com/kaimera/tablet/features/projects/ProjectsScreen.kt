package com.kaimera.tablet.features.projects

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
            TreeNode("archive", "Archive", Icons.Default.Folder)
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
                modifier = Modifier.width(250.dp)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is ProjectsUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is ProjectsUiState.Success -> {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Project: \${selectedNodeId.replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Mock Task List
                            Text(text = "Tasks", style = MaterialTheme.typography.titleLarge, modifier = Modifier.align(Alignment.Start))
                            Spacer(modifier = Modifier.height(8.dp))
                            repeat(3) { i ->
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Task, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(text = "Task \${i + 1} for \${selectedNodeId}")
                                    }
                                }
                            }
                        }
                    }
                    is ProjectsUiState.Error -> {
                        Text(text = "Error: \${state.message}")
                    }
                }
            }
        }
    }
}
