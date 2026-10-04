package nl.rhaydus.softcover.core.component.control

sealed interface ToggleEvent<K> {
    /** [checked] is the state the user asked for, the inverse of what the model showed. */
    data class Toggled<K>(
        val key: K,
        val checked: Boolean,
    ) : ToggleEvent<K>
}
