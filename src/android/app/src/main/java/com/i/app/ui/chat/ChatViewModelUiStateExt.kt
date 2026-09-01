package com.i.app.ui.chat

// [T-android-split-chat] Small UI-state toggle methods extracted from
// ChatViewModel as extension functions (verbatim): tool-detail sheet,
// browser sheet, memory sheet, attachment list. The 4 backing state fields
// were flipped private->internal. No logic change.

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.compose.foundation.lazy.LazyListState
import com.i.app.agent.Level
import com.i.app.agent.ToolLoopDetector
import com.i.app.browser.BrowserActionInput
import com.i.app.browser.BrowserTabPool
import com.i.app.data.db.MessageEntity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Extension
import com.i.app.data.BPETokenizer
import com.i.app.data.ContextOffload
import com.i.app.data.ContextPolicy
import com.i.app.logging.AppLogger
import com.i.app.data.FileMentionIndex
import com.i.app.data.db.CompactMarkerEntity
import com.i.app.data.model.AgentContentPart
import com.i.app.data.model.AgentToolDefinition
import com.i.app.data.model.LLMMessage
import com.i.app.data.model.LLMModel
import com.i.app.data.model.LLMStreamChunk
import com.i.app.data.model.LLMUsage
import com.i.app.data.model.ModelGroup
import com.i.app.data.model.ThinkingLevel
import com.i.app.R
import com.i.app.data.repository.ChatRepository
import com.i.app.data.repository.MemoryRepository
import com.i.app.data.repository.ProviderRepository
import com.i.app.provider.ImageBudget
import com.i.app.provider.LLMProvider
import com.i.app.provider.ProviderFactory
import com.i.app.sandbox.ExecutionCoordinator
import com.i.app.terminal.IOpenUrlBroker
import com.i.app.terminal.IUrlMarker
import com.i.app.tools.AgentTools
import com.i.app.tools.FileEditTool
import com.i.app.tools.FileReadTool
import com.i.app.tools.FileWriteTool
import com.i.app.tools.MemoryTools
import com.i.app.tools.ReadImageTool
import com.i.app.tools.ToolExecutionResult
import com.i.app.offload.OffloadPermissionManager
import com.i.app.service.SessionActivityTracker
import com.i.app.service.SessionConcurrencyManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import org.json.JSONObject
import java.io.ByteArrayOutputStream

internal fun ChatViewModel.openToolDetail(toolBlockId: String) {
    _selectedToolDetailId.value = toolBlockId
}

internal fun ChatViewModel.closeToolDetail() {
    _selectedToolDetailId.value = null
}

internal fun ChatViewModel.toggleBrowserSheet() {
    val opening = !_showBrowserSheet.value
    if (opening) browserTabPool.ensureTabForUI()
    _showBrowserSheet.value = opening
}

internal fun ChatViewModel.dismissBrowserSheet() {
    _showBrowserSheet.value = false
}

/**
 * Open the session browser sheet, focused on the tab whose URL matches
 * [url]. If no pool tab currently has that URL, a new tab is created and
 * loaded. Used by the tool-call preview's globe button so the agent's
 * existing browser_use page is reused when available instead of spawning
 * a duplicate tab.
 */
internal fun ChatViewModel.openBrowserSheetForUrl(url: String) {
    if (url.isBlank()) {
        browserTabPool.ensureTabForUI()
    } else {
        browserTabPool.selectOrCreateTabForURL(url)
    }
    _showBrowserSheet.value = true
}

internal fun ChatViewModel.toggleMemorySheet() {
    _showMemorySheet.value = !_showMemorySheet.value
}

internal fun ChatViewModel.dismissMemorySheet() {
    _showMemorySheet.value = false
}

internal fun ChatViewModel.addAttachment(attachment: InputAttachment) {
    _attachments.value = _attachments.value + attachment
}

internal fun ChatViewModel.removeAttachment(id: String) {
    _attachments.value = _attachments.value.filter { it.id != id }
}

internal fun ChatViewModel.clearAttachments() {
    _attachments.value = emptyList()
}
