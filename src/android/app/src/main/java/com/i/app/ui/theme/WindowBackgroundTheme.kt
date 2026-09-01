package com.i.app.ui.theme

internal const val LIGHT_WINDOW_BACKGROUND: Int = 0xFFFFFFFF.toInt()
internal const val DARK_WINDOW_BACKGROUND: Int = 0xFF000000.toInt()

internal fun windowBackgroundColor(darkTheme: Boolean): Int =
    if (darkTheme) DARK_WINDOW_BACKGROUND else LIGHT_WINDOW_BACKGROUND
