package nl.rhaydus.softcover.core.component.field

sealed interface TextFieldEvent {
    data class ValueChanged(
        val key: String,
        val text: String,
    ) : TextFieldEvent

    data class Submitted(val key: String) : TextFieldEvent

    data class TrailingClicked(val key: String) : TextFieldEvent
}
