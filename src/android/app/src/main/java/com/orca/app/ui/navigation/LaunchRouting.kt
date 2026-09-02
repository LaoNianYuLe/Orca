package com.orca.app.ui.navigation

internal enum class ColdStartDestination {
    SESSION_LIST,
    LAST_SESSION,
    NEW_CHAT,
}

/** Resolves cold-start behavior without creating a chat before the welcome page can render. */
internal fun resolveColdStartDestination(
    mode: Int,
    hasSessions: Boolean,
    latestSessionIsFresh: Boolean,
    hasPendingShare: Boolean,
    forceHome: Boolean,
): ColdStartDestination {
    if (forceHome) return ColdStartDestination.SESSION_LIST
    if (hasPendingShare && mode == 3) return ColdStartDestination.NEW_CHAT

    return when (mode) {
        1 -> if (hasSessions) ColdStartDestination.LAST_SESSION
        else ColdStartDestination.SESSION_LIST
        2 -> ColdStartDestination.NEW_CHAT
        3 -> ColdStartDestination.SESSION_LIST
        else -> when {
            !hasSessions -> ColdStartDestination.SESSION_LIST
            latestSessionIsFresh -> ColdStartDestination.LAST_SESSION
            else -> ColdStartDestination.NEW_CHAT
        }
    }
}
