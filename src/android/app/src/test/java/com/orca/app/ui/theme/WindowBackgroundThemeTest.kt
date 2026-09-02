package com.orca.app.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class WindowBackgroundThemeTest {
    @Test
    fun `dark app theme uses a dark window background`() {
        assertEquals(DARK_WINDOW_BACKGROUND, windowBackgroundColor(darkTheme = true))
    }

    @Test
    fun `light app theme uses a light window background`() {
        assertEquals(LIGHT_WINDOW_BACKGROUND, windowBackgroundColor(darkTheme = false))
    }
}
