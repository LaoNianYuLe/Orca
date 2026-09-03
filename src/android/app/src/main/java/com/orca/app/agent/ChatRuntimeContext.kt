package com.orca.app.agent

import com.orca.app.data.model.LLMModel

/**
 * Runtime-context suffix of the chat system prompt, plus the static rule that
 * separates SOUL persona from the API `model` field.
 *
 * The request body uses [LLMModel.id] as `"model"` (OpenAIProvider /
 * AnthropicProvider / GeminiProvider). This block copies that same string so
 * "what model are you?" can be answered without tools and without treating
 * an OpenAI-compatible wire protocol as the vendor.
 */
object ChatRuntimeContext {

    const val MODEL_ID_KEY = "chat_backend_model_id"
    const val MODEL_DISPLAY_KEY = "chat_backend_model_display_name"
    const val UNAVAILABLE = "unavailable"

    /** First-line / control-stripped cap. Catalog ids are short; a pasted
     *  config value must not become a prompt-injection paragraph. */
    const val MAX_TOKEN_CHARS = 256

    /**
     * Stable prefix (lives in `base`, not the dated suffix). Keep wording
     * byte-stable — any edit busts prompt-cache for the whole head.
     */
    val IDENTITY_VS_BACKEND_RULE =
        "Identity vs serving model:\n" +
            "- The identity sentence is your persona (SOUL.md name). That is who you speak as.\n" +
            "- This turn is served by the API model id in Runtime context as $MODEL_ID_KEY. " +
            "That string is copied from this request's `model` field. It is not inferred, " +
            "not a wire-protocol name (OpenAI-compatible / Anthropic / Gemini), and not the persona name.\n" +
            "- If the user asks what model you are, which model is answering, or for a model id, " +
            "answer with $MODEL_ID_KEY. If they ask who you are, use the persona name. " +
            "If $MODEL_ID_KEY is $UNAVAILABLE, say you do not have it. Do not guess. " +
            "Do not call orca-config or orca-model-use to look it up.\n\n"

    data class BackendModel(
        val id: String,
        val displayName: String? = null,
    )

    /**
     * Wire model for this turn: the [LLMModel] hanging on the provider that
     * is about to `streamMessage`, falling back to the ViewModel snapshot.
     * Never use ProviderType / instance label — those name the protocol or
     * the user's endpoint nickname, not the weights.
     */
    fun backendModelFrom(model: LLMModel?): BackendModel? {
        val id = sanitizeModelToken(model?.id) ?: return null
        val display = sanitizeModelToken(model?.displayName)?.takeIf { it != id }
        return BackendModel(id, display)
    }

    fun sanitizeModelToken(raw: String?): String? {
        if (raw == null) return null
        val firstLine = raw.lineSequence().firstOrNull().orEmpty()
        val cleaned = buildString(firstLine.length) {
            for (ch in firstLine) {
                if (ch.isISOControl()) continue
                append(ch)
            }
        }.trim()
        if (cleaned.isEmpty()) return null
        return if (cleaned.length <= MAX_TOKEN_CHARS) cleaned
        else cleaned.take(MAX_TOKEN_CHARS)
    }

    /**
     * Full Runtime context block, including the header. Field order is part
     * of the prompt-cache contract: date → tz (inline) → lang → model-use
     * count → backend id → optional display name. Do not reorder.
     */
    fun runtimeContextBlock(
        dateStr: String,
        tzId: String,
        lang: String,
        modelUseCount: Int,
        backend: BackendModel?,
    ): String = buildString {
        append("Runtime context:\n")
        append("- Current date: ").append(dateStr).append(" (").append(tzId).append(")\n")
        append("- Device language: ").append(lang).append("\n")
        append("- orca-model-use models available: ").append(modelUseCount).append("\n")
        val id = backend?.id ?: UNAVAILABLE
        append("- ").append(MODEL_ID_KEY).append(": ").append(id)
        val display = backend?.displayName
        if (display != null) {
            append("\n- ").append(MODEL_DISPLAY_KEY).append(": ").append(display)
        }
    }

    fun parseBackendModelId(systemPrompt: String?): String? {
        if (systemPrompt.isNullOrEmpty()) return null
        val prefix = "- $MODEL_ID_KEY: "
        val start = systemPrompt.lastIndexOf(prefix)
        if (start < 0) return null
        val from = start + prefix.length
        val end = systemPrompt.indexOf('\n', from).let { if (it < 0) systemPrompt.length else it }
        return systemPrompt.substring(from, end).trim().takeIf { it.isNotEmpty() }
    }
}
