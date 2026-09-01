package com.i.app.provider.catalog

enum class ModelSource {
    BUILTIN,
    REMOTE,
    CUSTOM,
}

data class ModelSpec(
    val id: String,
    val displayName: String,
    val providerId: String,
    val source: ModelSource,
    val type: String = "chat",
    val contextWindowTokens: Int? = null,
    val maxOutputTokens: Int? = null,
    val vision: Boolean = false,
    val files: Boolean = false,
    val functionCall: Boolean = false,
    val reasoning: Boolean = false,
    val search: Boolean = false,
    val audio: Boolean = false,
    val imageOutput: Boolean = false,
    val video: Boolean = false,
    val releasedAt: String? = null,
    val knowledgeCutoff: String? = null,
    val inputPricePerMillion: Double? = null,
    val outputPricePerMillion: Double? = null,
)
