package com.orca.app.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class LaunchRoutingRegressionTest {
    @Test
    fun `first launch with no sessions stays on welcome home`() {
        assertEquals(
            ColdStartDestination.SESSION_LIST,
            resolveColdStartDestination(
                mode = 0,
                hasSessions = false,
                latestSessionIsFresh = false,
                hasPendingShare = false,
                forceHome = false,
            ),
        )
    }

    @Test
    fun `auto launch with a fresh session reopens that session`() {
        assertEquals(
            ColdStartDestination.LAST_SESSION,
            resolveColdStartDestination(
                mode = 0,
                hasSessions = true,
                latestSessionIsFresh = true,
                hasPendingShare = false,
                forceHome = false,
            ),
        )
    }

    @Test
    fun `new chat launch mode still opens a new chat`() {
        assertEquals(
            ColdStartDestination.NEW_CHAT,
            resolveColdStartDestination(
                mode = 2,
                hasSessions = false,
                latestSessionIsFresh = false,
                hasPendingShare = false,
                forceHome = false,
            ),
        )
    }
}
