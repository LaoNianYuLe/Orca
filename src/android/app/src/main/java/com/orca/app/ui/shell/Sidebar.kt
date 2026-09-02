package com.orca.app.ui.shell

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.LibraryBooks
import androidx.compose.material.icons.outlined.NoteAdd
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orca.app.R
import com.orca.app.data.db.ChatSessionEntity

private data class SidebarAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

internal const val SIDEBAR_SETTINGS_BOTTOM_INSET_DP = 20

internal enum class SidebarActionId {
    NEW_CHAT,
    STORAGE,
    PROJECTS,
    SKILLS,
    COPYWRITING,
    SETTINGS,
}

internal fun sidebarActionIds(): List<SidebarActionId> = listOf(
    SidebarActionId.NEW_CHAT,
    SidebarActionId.STORAGE,
    SidebarActionId.PROJECTS,
    SidebarActionId.SKILLS,
    SidebarActionId.COPYWRITING,
    SidebarActionId.SETTINGS,
)

@Composable
fun Sidebar(
    sessions: List<ChatSessionEntity>,
    onNewChat: () -> Unit,
    onStorage: () -> Unit,
    onProjects: () -> Unit,
    onSkills: () -> Unit,
    onCopywriting: () -> Unit,
    onSettings: () -> Unit,
    onSessionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val actions = listOf(
        SidebarAction(stringResource(R.string.sidebar_new_chat), Icons.Outlined.NoteAdd, onNewChat),
        SidebarAction(stringResource(R.string.sidebar_phone_library), Icons.Outlined.Storage, onStorage),
        SidebarAction(stringResource(R.string.sidebar_projects), Icons.Outlined.Folder, onProjects),
        SidebarAction(stringResource(R.string.sidebar_skills), Icons.Outlined.LibraryBooks, onSkills),
        SidebarAction(stringResource(R.string.sidebar_copywriting), Icons.Outlined.AutoAwesome, onCopywriting),
    )
    val recentSessions = sessions
        .sortedByDescending { it.updatedAt }
        .take(20)

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(top = 8.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        actions.forEach { action ->
            SidebarActionRow(action)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        Text(
            text = stringResource(R.string.sidebar_recent),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(recentSessions, key = { it.id }) { session ->
                SidebarSessionItem(session = session, onClick = onSessionClick)
            }
        }
        Divider(modifier = Modifier.padding(horizontal = 16.dp))
        SidebarActionRow(
            SidebarAction(stringResource(R.string.sidebar_settings), Icons.Outlined.Settings, onSettings),
            modifier = Modifier.padding(bottom = SIDEBAR_SETTINGS_BOTTOM_INSET_DP.dp),
        )
    }
}

@Composable
private fun SidebarActionRow(action: SidebarAction, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(onClick = action.onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = action.icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = action.label,
            modifier = Modifier
                .padding(start = 16.dp)
                .width(220.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
