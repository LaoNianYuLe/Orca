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
        assertEquals(true, Routes.isSettingsRoute("storage"))
        assertEquals(true, Routes.isSettingsRoute("session_storage/abc"))
        assertEquals(true, Routes.isSettingsRoute("terminal"))
        assertEquals(true, Routes.isSettingsRoute("terminal?initCommand=ls"))
        assertEquals(true, Routes.isSettingsRoute("projects"))
        assertEquals(true, Routes.isSettingsRoute("copywriting"))
        assertEquals(true, Routes.isSettingsRoute("skill/skill-creator"))
        assertEquals(false, Routes.isSettingsRoute(Routes.CHAT))
        assertEquals(false, Routes.isSettingsRoute(Routes.chat("abc")))
        assertEquals(false, Routes.isSettingsRoute(Routes.SESSION_LIST))
    }

    @Test
    fun settingsActionKeepsAComfortableBottomInset() {
        assertEquals(20, SIDEBAR_SETTINGS_BOTTOM_INSET_DP)
    }
}
