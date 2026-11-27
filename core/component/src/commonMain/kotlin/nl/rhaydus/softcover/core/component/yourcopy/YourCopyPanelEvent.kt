package nl.rhaydus.softcover.core.component.yourcopy

sealed interface YourCopyPanelEvent<out K> {
    data class RowClicked<K>(val key: K) : YourCopyPanelEvent<K>
}
