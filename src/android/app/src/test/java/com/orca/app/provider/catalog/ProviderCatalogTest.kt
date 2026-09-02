package com.orca.app.provider.catalog

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
            "Poolside",
            "Inception",
        )

        assertEquals(expected, ProviderCatalog.builtIn.map { it.displayName })
        assertEquals((1..22).toList(), ProviderCatalog.builtIn.map { it.displayOrder })
        assertEquals(22, ProviderCatalog.builtIn.map { it.id }.distinct().size)
    }

    @Test
    fun `catalog exposes verified protocol and credential metadata`() {
        val openAi = ProviderCatalog.require("openai")
        val ollama = ProviderCatalog.require("ollama")
        val poolside = ProviderCatalog.require("poolside")
        val inception = ProviderCatalog.require("inception")

        assertEquals(ProviderProtocol.OPENAI_COMPATIBLE, openAi.protocol)
        assertEquals(ProviderCredentialMode.API_KEY, openAi.credentialMode)
        assertEquals(ProviderProtocol.OLLAMA, ollama.protocol)
        assertEquals(ProviderCredentialMode.LOCAL, ollama.credentialMode)
        assertEquals(ProviderProtocol.OPENAI_COMPATIBLE, poolside.protocol)
        assertEquals(ProviderCredentialMode.API_KEY, poolside.credentialMode)
        assertNotNull(poolside.modelFetcher)
        assertEquals(ProviderProtocol.OPENAI_COMPATIBLE, inception.protocol)
        assertEquals(ProviderCredentialMode.API_KEY, inception.credentialMode)
        assertNotNull(inception.modelFetcher)
        assertTrue(ProviderCatalog.find("Stealth") == null)
        assertNotNull(ProviderCatalog.find("OpenAI"))
    }
}
