package nl.rhaydus.softcover.core.component.sheet

sealed interface SheetRowEvent<out K> {
    data class Selected<K>(val key: K) : SheetRowEvent<K>
}
