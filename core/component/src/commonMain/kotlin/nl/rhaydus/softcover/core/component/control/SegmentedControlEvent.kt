package nl.rhaydus.softcover.core.component.control

sealed interface SegmentedControlEvent<K> {
    data class Selected<K>(val key: K) : SegmentedControlEvent<K>
}
