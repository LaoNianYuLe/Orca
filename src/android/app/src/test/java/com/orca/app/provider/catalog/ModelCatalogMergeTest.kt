package com.orca.app.provider.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ModelCatalogMergeTest {
    @Test
    fun `remote refresh updates remote models but preserves custom models`() {
        val builtin = listOf(
            ModelSpec(id = "alpha", displayName = "Alpha", providerId = "openai", source = ModelSource.BUILTIN),
        )
        val existing = listOf(
            ModelSpec(id = "alpha", displayName = "Old Alpha", providerId = "openai", source = ModelSource.REMOTE),
            ModelSpec(id = "custom", displayName = "My Model", providerId = "openai", source = ModelSource.CUSTOM),
        )
        val remote = listOf(
            ModelSpec(id = "alpha", displayName = "New Alpha", providerId = "openai", source = ModelSource.REMOTE, contextWindowTokens = 128_000),
            ModelSpec(id = "beta", displayName = "Beta", providerId = "openai", source = ModelSource.REMOTE),
        )

        val merged = ModelCatalogMerger.merge(builtin, existing, remote)

        assertEquals(listOf("alpha", "custom", "beta"), merged.map { it.id })
        assertEquals("New Alpha", merged.first { it.id == "alpha" }.displayName)
        assertEquals("My Model", merged.first { it.id == "custom" }.displayName)
        assertEquals(128_000, merged.first { it.id == "alpha" }.contextWindowTokens)
        assertTrue(merged.none { it.id == "alpha" && it.source == ModelSource.CUSTOM })
    }

    @Test
    fun `provider id and model id form the merge key`() {
        val first = ModelSpec("same", "OpenAI Same", "openai", ModelSource.REMOTE)
        val second = ModelSpec("same", "Other Provider Same", "anthropic", ModelSource.REMOTE)

        val merged = ModelCatalogMerger.merge(emptyList(), emptyList(), listOf(first, second))

        assertEquals(2, merged.size)
    }
}
