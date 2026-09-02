package com.orca.app.ui.shell

enum class SidebarTarget {
    Closed,
    Open,
}

object SidebarState {
    private const val FLING_VELOCITY_THRESHOLD = 1_000f

    fun targetAfterDrag(offset: Float, width: Float, velocity: Float): SidebarTarget {
        if (velocity >= FLING_VELOCITY_THRESHOLD) return SidebarTarget.Open
        if (velocity <= -FLING_VELOCITY_THRESHOLD) return SidebarTarget.Closed
        return if (offset >= width * 0.5f) SidebarTarget.Open else SidebarTarget.Closed
    }
}
