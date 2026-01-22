package com.kaimera.tablet.features.notes

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kaimera.tablet.core.ui.components.TreeNode
import com.kaimera.tablet.core.ui.components.TreePanel
import com.kaimera.tablet.core.ui.components.NavDrawerTreePanel
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Menu
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(onBack: () -> Unit) {
    var noteText by remember { mutableStateOf("New Note\n\n- Task 1\n- Task 2") }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val treeNodes = remember {
        listOf(
            TreeNode("root", "My Notes", Icons.Default.Description, children = listOf(
                TreeNode("personal", "Personal", Icons.Default.Label),
                TreeNode("work", "Work", Icons.Default.Label),
                TreeNode("ideas", "Ideas", Icons.Default.Note)
            )),
            TreeNode("archive", "Archive", Icons.Default.History),
            TreeNode("trash", "Trash", Icons.Default.Star)
        )
    }
    var selectedNodeId by remember { mutableStateOf("personal") }

    NavDrawerTreePanel(
        drawerState = drawerState,
        title = "Notes",
        onHomeClick = onBack,
        nodes = treeNodes,
        selectedNodeId = selectedNodeId,

        onNodeSelected = { selectedNodeId = it.id }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Notes") },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },

                    actions = {
                        IconButton(onClick = { /* Auto-saved in state for now */ }) {
                            Icon(Icons.Default.Save, contentDescription = "Save")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                TextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier.fillMaxSize(),
                    placeholder = { Text("Start typing...") },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    }
}

