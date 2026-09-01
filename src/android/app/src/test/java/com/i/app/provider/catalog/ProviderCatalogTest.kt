package com.i.app.provider.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderCatalogTest {
    @Test
    fun `built in providers keep the approved display order`() {
        val expected = listOf(
            "OpenAI",
            "Google",
            "Anthropic",
            "DeepSeek",
            "Meta",
            "阿里云 / 通义千问",
            "xAI",
            "OpenRouter",
            "Microsoft Azure",
            "字节跳动 / 火山引擎",
            "腾讯云 / 混元",
            "百度 / 文心 / 千帆",
            "Amazon Bedrock",
            "GitHub Models / Copilot",
            "智谱 Z.ai",
            "月之暗面 Moonshot",
            "NVIDIA",
            "MiniMax",
            "小米 MiMo",
            "Ollama",
            "Stealth",
            "Poolside",
            "Inception",
        )

        assertEquals(expected, ProviderCatalog.builtIn.map { it.displayName })
        assertEquals((1..23).toList(), ProviderCatalog.builtIn.map { it.displayOrder })
        assertEquals(23, ProviderCatalog.builtIn.map { it.id }.distinct().size)
    }

    @Test
    fun `catalog exposes protocol and credential metadata without guessing unverified APIs`() {
        val openAi = ProviderCatalog.require("openai")
        val ollama = ProviderCatalog.require("ollama")
        val stealth = ProviderCatalog.require("stealth")

        assertEquals(ProviderProtocol.OPENAI_COMPATIBLE, openAi.protocol)
        assertEquals(ProviderCredentialMode.API_KEY, openAi.credentialMode)
        assertEquals(ProviderProtocol.OLLAMA, ollama.protocol)
        assertEquals(ProviderCredentialMode.LOCAL, ollama.credentialMode)
        assertEquals(ProviderProtocol.UNVERIFIED, stealth.protocol)
        assertEquals(ProviderCredentialMode.API_KEY, stealth.credentialMode)
        assertTrue(stealth.modelFetcher == null)
        assertNotNull(ProviderCatalog.find("OpenAI"))
    }
}
