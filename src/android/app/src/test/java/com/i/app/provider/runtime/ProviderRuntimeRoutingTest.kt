package com.i.app.provider.runtime

import com.i.app.provider.catalog.ProviderCatalog
import com.i.app.provider.catalog.ProviderProtocol
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProviderRuntimeRoutingTest {
    @Test
    fun `every built in provider resolves to one shared runtime category`() {
        val bindings = ProviderCatalog.builtIn.map { ProviderRuntimeResolver.resolve(it) }

        assertEquals(22, bindings.size)
        assertTrue(bindings.all { it.runtime != ProviderRuntimeKind.UNSUPPORTED })
    }

    @Test
    fun `openai compatible providers share one runtime`() {
        val compatible = ProviderCatalog.builtIn
            .filter { it.protocol == ProviderProtocol.OPENAI_COMPATIBLE }
            .map { ProviderRuntimeResolver.resolve(it) }

        assertTrue(compatible.size > 1)
        assertTrue(compatible.all { it.runtime == ProviderRuntimeKind.OPENAI_COMPATIBLE })
        assertEquals(
            ProviderRuntimeKind.OPENAI_COMPATIBLE,
            ProviderRuntimeResolver.resolve(ProviderCatalog.require("poolside")).runtime,
        )
        assertEquals(
            ProviderRuntimeKind.OPENAI_COMPATIBLE,
            ProviderRuntimeResolver.resolve(ProviderCatalog.require("inception")).runtime,
        )
    }
}
