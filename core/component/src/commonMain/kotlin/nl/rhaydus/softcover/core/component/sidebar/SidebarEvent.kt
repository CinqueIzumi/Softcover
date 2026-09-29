package nl.rhaydus.softcover.core.component.sidebar

sealed interface SidebarEvent<out K> {
    data class Clicked<K>(val key: K) : SidebarEvent<K>
    data class LongPressed<K>(val key: K) : SidebarEvent<K>
}
