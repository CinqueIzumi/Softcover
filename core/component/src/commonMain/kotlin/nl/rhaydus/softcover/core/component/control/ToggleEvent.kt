package nl.rhaydus.softcover.core.component.control

sealed interface ToggleEvent {
    /** [checked] is the state the user asked for, the inverse of what the model showed. */
    data class Toggled(
        val key: String,
        val checked: Boolean,
    ) : ToggleEvent
}
