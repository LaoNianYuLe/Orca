package com.i.app.ui.shell

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.i.app.data.db.ChatSessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppShell(
    sessions: Flow<List<ChatSessionEntity>>,
    onNewChat: () -> Unit,
    onStorage: () -> Unit,
    onProjects: () -> Unit,
    onSkills: () -> Unit,
    onCopywriting: () -> Unit,
    onSessionClick: (String) -> Unit,
    content: @Composable () -> Unit,
) {
    val recentSessions by sessions.collectAsState(initial = emptyList())
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val drawerFraction = remember { Animatable(-1f) }
    var rootWidthPx by remember { mutableFloatStateOf(0f) }
    val drawerWidthPx = if (rootWidthPx <= 0f) {
        0f
    } else {
        (rootWidthPx * 0.82f).coerceIn(
            with(density) { 280.dp.toPx() },
            rootWidthPx,
        )
    }
    val drawerIsOpen = drawerFraction.value > -0.99f

    fun settle(target: SidebarTarget) {
        scope.launch {
            drawerFraction.animateTo(
                targetValue = if (target == SidebarTarget.Open) 0f else -1f,
                animationSpec = tween(durationMillis = 250),
            )
        }
    }

    BackHandler(enabled = drawerIsOpen) {
        settle(SidebarTarget.Closed)
    }

    LaunchedEffect(drawerWidthPx) {
        if (drawerWidthPx <= 1f) return@LaunchedEffect
        drawerFraction.snapTo(drawerFraction.value.coerceIn(-1f, 0f))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { rootWidthPx = it.width.toFloat() }
            .pointerInput(drawerIsOpen, drawerWidthPx) {
                var fromEdge = false
                detectDragGestures(
                    onDragStart = { fromEdge = it.x <= with(density) { 32.dp.toPx() } },
                    onDragEnd = {
                        if (fromEdge && !drawerIsOpen) settle(SidebarTarget.Open)
                    },
                    onDragCancel = { fromEdge = false },
                    onDrag = { change, dragAmount ->
                        if (!fromEdge || drawerIsOpen || drawerWidthPx <= 1f) return@detectDragGestures
                        change.consume()
                        scope.launch {
                            drawerFraction.snapTo(
                                (drawerFraction.value + dragAmount.x / drawerWidthPx)
                                    .coerceIn(-1f, 0f),
                            )
                        }
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = (-drawerFraction.value).coerceIn(0f, 1f) *
                        with(density) { 12.dp.toPx() }
                },
        ) {
            content()
            if (!drawerIsOpen) {
                IconButton(
                    onClick = { settle(SidebarTarget.Open) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .statusBarsPadding()
                        .background(MaterialTheme.colorScheme.background)
                        .zIndex(2f)
                        .semantics { contentDescription = "Open sidebar" },
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        if (drawerIsOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.34f))
                    .clickable { settle(SidebarTarget.Closed) }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = { settle(SidebarTarget.Closed) },
                            onDrag = { change, _ -> change.consume() },
                        )
                    }
                    .zIndex(3f),
            )
        }

        Box(
            modifier = Modifier
                .offset { IntOffset((drawerFraction.value * drawerWidthPx).roundToInt(), 0) }
                .width(with(density) { drawerWidthPx.toDp() })
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(drawerWidthPx) {
                    detectDragGestures(
                        onDragEnd = { settle(SidebarTarget.Closed) },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (drawerWidthPx > 1f) {
                                scope.launch {
                                    drawerFraction.snapTo(
                                        (drawerFraction.value + dragAmount.x / drawerWidthPx)
                                            .coerceIn(-1f, 0f),
                                    )
                                }
                            }
                        },
                    )
                }
                .zIndex(4f),
        ) {
            Sidebar(
                sessions = recentSessions,
                onNewChat = { settle(SidebarTarget.Closed); onNewChat() },
                onStorage = { settle(SidebarTarget.Closed); onStorage() },
                onProjects = { settle(SidebarTarget.Closed); onProjects() },
                onSkills = { settle(SidebarTarget.Closed); onSkills() },
                onCopywriting = { settle(SidebarTarget.Closed); onCopywriting() },
                onSessionClick = { id -> settle(SidebarTarget.Closed); onSessionClick(id) },
            )
        }
    }
}
