package com.orca.app.provider.runtime

import com.orca.app.provider.catalog.ProviderCredentialMode
import com.orca.app.provider.catalog.ProviderProtocol
import com.orca.app.provider.catalog.ProviderSpec

/**
 * Shared protocol boundary for provider integrations.
 *
 * A provider entry describes product data (name, credentials and endpoint),
 * while this layer selects the implementation family. This prevents every
 * OpenAI-compatible vendor from growing another copy of the same transport
 * code.
 */
enum class ProviderRuntimeKind {
    OPENAI_COMPATIBLE,
    ANTHROPIC,
    GEMINI,
    OLLAMA,
    AZURE_OPENAI,
    BEDROCK,
    GITHUB_COPILOT,
    UNSUPPORTED,
}

data class ProviderRuntimeBinding(
    val providerId: String,
    val protocol: ProviderProtocol,
    val credentialMode: ProviderCredentialMode,
    val defaultBaseUrl: String?,
    val runtime: ProviderRuntimeKind,
)

object ProviderRuntimeResolver {
    fun resolve(spec: ProviderSpec): ProviderRuntimeBinding = ProviderRuntimeBinding(
        providerId = spec.id,
        protocol = spec.protocol,
        credentialMode = spec.credentialMode,
        defaultBaseUrl = spec.defaultBaseUrl,
        runtime = spec.protocol.toRuntimeKind(),
    )

    private fun ProviderProtocol.toRuntimeKind(): ProviderRuntimeKind = when (this) {
        ProviderProtocol.OPENAI_COMPATIBLE -> ProviderRuntimeKind.OPENAI_COMPATIBLE
        ProviderProtocol.ANTHROPIC -> ProviderRuntimeKind.ANTHROPIC
        ProviderProtocol.GEMINI -> ProviderRuntimeKind.GEMINI
        ProviderProtocol.OLLAMA -> ProviderRuntimeKind.OLLAMA
        ProviderProtocol.AZURE_OPENAI -> ProviderRuntimeKind.AZURE_OPENAI
        ProviderProtocol.BEDROCK -> ProviderRuntimeKind.BEDROCK
        ProviderProtocol.GITHUB_COPILOT -> ProviderRuntimeKind.GITHUB_COPILOT
        ProviderProtocol.UNVERIFIED -> ProviderRuntimeKind.UNSUPPORTED
    }
}
