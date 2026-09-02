package com.orca.app.ui.shell

import org.junit.Assert.assertEquals
import org.junit.Test

class SidebarStateTest {
    @Test
    fun `release past midpoint opens drawer`() {
        assertEquals(
            SidebarTarget.Open,
            SidebarState.targetAfterDrag(offset = 500f, width = 800f, velocity = 0f),
        )
    }

    @Test
    fun `release before midpoint closes drawer`() {
        assertEquals(
            SidebarTarget.Closed,
            SidebarState.targetAfterDrag(offset = 200f, width = 800f, velocity = 0f),
        )
    }

    @Test
    fun `fling direction wins over drag distance`() {
        assertEquals(SidebarTarget.Open, SidebarState.targetAfterDrag(80f, 800f, 1_200f))
        assertEquals(SidebarTarget.Closed, SidebarState.targetAfterDrag(720f, 800f, -1_200f))
    }
}
