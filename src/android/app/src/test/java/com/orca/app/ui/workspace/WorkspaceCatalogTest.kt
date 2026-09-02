package com.orca.app.ui.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkspaceCatalogTest {
    @Test
    fun `copywriting catalog exposes four usable Chinese templates`() {
        assertEquals(4, copywritingTemplates.size)
        assertEquals(
            listOf("短视频文案", "社交媒体文案", "产品介绍", "文章提纲"),
            copywritingTemplates.map { it.title },
        )
        assertTrue(copywritingTemplates.all { it.prompt.isNotBlank() })
    }
}
