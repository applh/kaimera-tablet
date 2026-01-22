package com.kaimera.tablet.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class TreeNode(
    val id: String,
    val label: String,
    val icon: ImageVector? = null,
    val children: List<TreeNode> = emptyList()
)

@Composable
fun TreePanel(
    nodes: List<TreeNode>,
    selectedNodeId: String?,
    onNodeSelected: (TreeNode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 1.dp
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(nodes) { node ->
                TreeRow(
                    node = node,
                    selectedNodeId = selectedNodeId,
                    onNodeSelected = onNodeSelected,
                    depth = 0
                )
            }
        }
    }
}

@Composable
fun TreeRow(
    node: TreeNode,
    selectedNodeId: String?,
    onNodeSelected: (TreeNode) -> Unit,
    depth: Int
) {
    var isExpanded by remember { mutableStateOf(false) }
    val isSelected = selectedNodeId == node.id
    val hasChildren = node.children.isNotEmpty()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = (depth * 16).dp)
                .clickable {
                    if (hasChildren) {
                        isExpanded = !isExpanded
                    }
                    onNodeSelected(node)
                }
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (hasChildren) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Spacer(modifier = Modifier.size(20.dp))
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = node.icon ?: Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = node.label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        if (hasChildren) {
            AnimatedVisibility(visible = isExpanded) {
                Column {
                    node.children.forEach { child ->
                        TreeRow(
                            node = child,
                            selectedNodeId = selectedNodeId,
                            onNodeSelected = onNodeSelected,
                            depth = depth + 1
                        )
                    }
                }
            }
        }
    }
}
