package com.kaimera.tablet.core.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@Composable
fun NavDrawerTreePanel(
    drawerState: DrawerState,
    title: String,
    onHomeClick: () -> Unit,
    nodes: List<TreeNode>,
    selectedNodeId: String?,
    onNodeSelected: (TreeNode) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {

    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                // Drawer Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onHomeClick) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                    }
                }
                
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                TreePanel(

                    nodes = nodes,
                    selectedNodeId = selectedNodeId,
                    onNodeSelected = { node ->
                        onNodeSelected(node)
                        scope.launch {
                            drawerState.close()
                        }
                    },
                    modifier = Modifier
                )
            }
        },
        modifier = modifier,
        content = content
    )
}
