package com.i.app.ui.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.i.app.R
import com.i.app.data.db.ChatSessionEntity
import com.i.app.data.db.FolderEntity
import com.i.app.data.repository.ChatRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
    chatRepository: ChatRepository,
    onBack: () -> Unit,
    onNewChatInProject: (String) -> Unit,
) {
    val folders by chatRepository.observeFolders().collectAsState(initial = emptyList())
    val sessions by chatRepository.observeSessions().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showCreate by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workspace_projects_title)) },
                actions = {
                    IconButton(onClick = { showCreate = true }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.workspace_create_project))
                    }
                },
            )
        },
    ) { padding ->
        if (folders.isEmpty()) {
            WorkspaceEmptyState(
                modifier = Modifier.padding(padding),
                icon = Icons.Default.Folder,
                title = stringResource(R.string.workspace_projects_empty_title),
                description = stringResource(R.string.workspace_projects_empty_description),
                actionLabel = stringResource(R.string.workspace_create_project),
                onAction = { showCreate = true },
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = stringResource(R.string.workspace_projects_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                items(folders, key = { it.id }) { folder ->
                    val count = sessions.count { it.folderId == folder.id }
                    ProjectCard(folder, count, onNewChatInProject)
                }
            }
        }
    }

    if (showCreate) {
        CreateProjectDialog(
            onDismiss = { showCreate = false },
            onCreate = { name, description ->
                scope.launch { chatRepository.createFolder(name, description) }
                showCreate = false
            },
        )
    }
}

@Composable
private fun ProjectCard(folder: FolderEntity, sessionCount: Int, onNewChat: (String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(folder.name, Modifier.padding(start = 12.dp).weight(1f), fontWeight = FontWeight.SemiBold)
            }
            folder.description?.let {
                Text(it, Modifier.padding(top = 8.dp), maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                stringResource(R.string.workspace_project_session_count, sessionCount),
                Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(onClick = { onNewChat(folder.id) }, Modifier.padding(top = 10.dp)) {
                Icon(Icons.Default.Edit, contentDescription = null, Modifier.size(18.dp))
                Text(stringResource(R.string.workspace_start_project_chat), Modifier.padding(start = 8.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CopywritingScreen(
    chatRepository: ChatRepository,
    onBack: () -> Unit,
    onNewCopywritingChat: (String) -> Unit,
    onOpenSession: (String) -> Unit,
) {
    val sessions by chatRepository.observeSessions().collectAsState(initial = emptyList())
    val writingSessions = sessions.filter { it.category.equals("writing", ignoreCase = true) }.take(10)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.workspace_copywriting_title)) },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(stringResource(R.string.workspace_copywriting_subtitle), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            items(copywritingTemplates, key = { it.title }) { template ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onNewCopywritingChat(template.prompt) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(template.title, fontWeight = FontWeight.SemiBold)
                            Text(template.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text(stringResource(R.string.workspace_copywriting_recent), fontWeight = FontWeight.SemiBold)
            }
            if (writingSessions.isEmpty()) {
                item { Text(stringResource(R.string.workspace_copywriting_no_history), color = MaterialTheme.colorScheme.onSurfaceVariant) }
            } else {
                items(writingSessions, key = { it.id }) { session ->
                    SessionHistoryRow(session, onClick = { onOpenSession(session.id) })
                }
            }
        }
    }
}

@Composable
private fun SessionHistoryRow(session: ChatSessionEntity, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(session.title ?: stringResource(R.string.chat_title_pill_session_default), Modifier.padding(start = 12.dp).weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun WorkspaceEmptyState(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    actionLabel: String,
    onAction: () -> Unit,
) {
    Column(modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(icon, contentDescription = null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
        Text(title, Modifier.padding(top = 16.dp), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(description, Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Button(onClick = onAction, Modifier.padding(top = 20.dp)) { Text(actionLabel) }
    }
}

@Composable
private fun CreateProjectDialog(onDismiss: () -> Unit, onCreate: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.workspace_create_project)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.workspace_project_name)) }, singleLine = true)
                OutlinedTextField(description, { description = it }, label = { Text(stringResource(R.string.workspace_project_description)) }, maxLines = 3)
            }
        },
        confirmButton = {
            Button(onClick = { onCreate(name, description) }, enabled = name.isNotBlank()) { Text(stringResource(R.string.create)) }
        },
        dismissButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}
