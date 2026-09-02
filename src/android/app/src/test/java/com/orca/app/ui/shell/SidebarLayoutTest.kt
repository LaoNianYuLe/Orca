package com.orca.app.ui.shell

import org.junit.Assert.assertEquals
import org.junit.Test
import com.orca.app.ui.navigation.Routes

class SidebarLayoutTest {
    @Test
    fun settingsIsTheBottomSidebarAction() {
        assertEquals(SidebarActionId.SETTINGS, sidebarActionIds().last())
    }

    @Test
    fun settingsChildRoutesHideGlobalMenuButton() {
        assertEquals(true, Routes.isSettingsRoute(Routes.SETTINGS))
        assertEquals(true, Routes.isSettingsRoute(Routes.MODEL_GROUPS))
        assertEquals(false, Routes.isSettingsRoute(Routes.CHAT))
    }

    @Test
    fun settingsActionKeepsAComfortableBottomInset() {
        assertEquals(20, SIDEBAR_SETTINGS_BOTTOM_INSET_DP)
    }
}
