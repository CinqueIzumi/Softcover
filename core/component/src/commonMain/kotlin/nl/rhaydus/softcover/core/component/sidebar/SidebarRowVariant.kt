package nl.rhaydus.softcover.core.component.sidebar

sealed interface SidebarRowVariant {
    data class Destination(
        val selected: Boolean,
        val longPressable: Boolean = false,
    ) : SidebarRowVariant

    data object Push : SidebarRowVariant
}
