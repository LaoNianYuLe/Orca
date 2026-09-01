package com.i.app.provider.catalog

enum class ProviderProtocol {
    OPENAI_COMPATIBLE,
    ANTHROPIC,
    GEMINI,
    OLLAMA,
    AZURE_OPENAI,
    BEDROCK,
    GITHUB_COPILOT,
    UNVERIFIED,
}

enum class ProviderCredentialMode {
    API_KEY,
    OAUTH,
    AWS,
    LOCAL,
}

data class ProviderSpec(
    val id: String,
    val displayName: String,
    val displayOrder: Int,
    val protocol: ProviderProtocol,
    val credentialMode: ProviderCredentialMode,
    val defaultBaseUrl: String? = null,
    val modelFetcher: ModelFetcherSpec? = null,
    val allowManualModels: Boolean = true,
)

data class ModelFetcherSpec(
    val path: String,
    val requiresApiKey: Boolean = true,
)
