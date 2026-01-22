package com.kaimera.tablet.features.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
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
fun CalendarScreen(
    onBack: () -> Unit = {},
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val treeNodes = remember {
        listOf(
            TreeNode("personal", "Personal", Icons.Default.Person),
            TreeNode("work", "Work", Icons.Default.Work),
            TreeNode("shared", "Shared", Icons.Default.Group)
        )
    }
    var selectedNodeId by remember { mutableStateOf("personal") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar") },
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
                modifier = Modifier.width(200.dp)
            )

            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (val state = uiState) {
                    is CalendarUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is CalendarUiState.Success -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Viewing \${selectedNodeId.replaceFirstChar { it.uppercase() }} Calendar",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                    }
                    is CalendarUiState.Error -> {
                        Text(text = "Error: \${state.message}")
                    }
                }
            }
        }
    }
}
