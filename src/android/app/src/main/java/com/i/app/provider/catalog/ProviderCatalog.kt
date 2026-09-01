package com.i.app.provider.catalog

/**
 * Built-in provider registry. Display order is product data and must not be
 * inferred from enum order or from the order returned by a remote API.
 */
object ProviderCatalog {
    val builtIn: List<ProviderSpec> = listOf(
        ProviderSpec("openai", "OpenAI", 1, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, "https://api.openai.com/v1", ModelFetcherSpec("/models")),
        ProviderSpec("google", "Google", 2, ProviderProtocol.GEMINI, ProviderCredentialMode.API_KEY, "https://generativelanguage.googleapis.com", ModelFetcherSpec("/v1beta/models")),
        ProviderSpec("anthropic", "Anthropic", 3, ProviderProtocol.ANTHROPIC, ProviderCredentialMode.API_KEY, "https://api.anthropic.com", ModelFetcherSpec("/v1/models")),
        ProviderSpec("deepseek", "DeepSeek", 4, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, "https://api.deepseek.com/v1", ModelFetcherSpec("/models")),
        ProviderSpec("meta", "Meta", 5, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, null, null),
        ProviderSpec("qwen", "阿里云 / 通义千问", 6, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, null, null),
        ProviderSpec("xai", "xAI", 7, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, "https://api.x.ai/v1", ModelFetcherSpec("/models")),
        ProviderSpec("openrouter", "OpenRouter", 8, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, "https://openrouter.ai/api/v1", ModelFetcherSpec("/models")),
        ProviderSpec("azure", "Microsoft Azure", 9, ProviderProtocol.AZURE_OPENAI, ProviderCredentialMode.API_KEY, null, null),
        ProviderSpec("volcengine", "字节跳动 / 火山引擎", 10, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, null, null),
        ProviderSpec("tencent", "腾讯云 / 混元", 11, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, null, null),
        ProviderSpec("wenxin", "百度 / 文心 / 千帆", 12, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, null, null),
        ProviderSpec("bedrock", "Amazon Bedrock", 13, ProviderProtocol.BEDROCK, ProviderCredentialMode.AWS, null, null),
        ProviderSpec("github", "GitHub Models / Copilot", 14, ProviderProtocol.GITHUB_COPILOT, ProviderCredentialMode.OAUTH, null, null),
        ProviderSpec("zhipu", "智谱 Z.ai", 15, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, "https://open.bigmodel.cn/api/paas/v4", ModelFetcherSpec("/models")),
        ProviderSpec("moonshot", "月之暗面 Moonshot", 16, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, "https://api.moonshot.cn/v1", ModelFetcherSpec("/models")),
        ProviderSpec("nvidia", "NVIDIA", 17, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, "https://integrate.api.nvidia.com/v1", ModelFetcherSpec("/models")),
        ProviderSpec("minimax", "MiniMax", 18, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, null, null),
        ProviderSpec("xiaomi-mimo", "小米 MiMo", 19, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, null, null),
        ProviderSpec("ollama", "Ollama", 20, ProviderProtocol.OLLAMA, ProviderCredentialMode.LOCAL, "http://localhost:11434", ModelFetcherSpec("/api/tags", requiresApiKey = false)),
        ProviderSpec("poolside", "Poolside", 21, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, "https://inference.poolside.ai/openai/v1", ModelFetcherSpec("/models")),
        ProviderSpec("inception", "Inception", 22, ProviderProtocol.OPENAI_COMPATIBLE, ProviderCredentialMode.API_KEY, "https://api.inceptionlabs.ai/v1", ModelFetcherSpec("/models")),
    )

    private val byId = builtIn.associateBy { it.id }
    private val byDisplayName = builtIn.associateBy { it.displayName.lowercase() }

    fun find(idOrName: String): ProviderSpec? =
        byId[idOrName] ?: byDisplayName[idOrName.trim().lowercase()]

    fun require(idOrName: String): ProviderSpec =
        find(idOrName) ?: error("Unknown built-in provider: $idOrName")
}
