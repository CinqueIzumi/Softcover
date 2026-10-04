package nl.rhaydus.softcover.core.component.button

sealed interface PillButtonEvent {
    val key: String

    data class Clicked(override val key: String) : PillButtonEvent
}
