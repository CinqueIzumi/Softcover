package nl.rhaydus.softcover.core.component.button

sealed interface PillButtonEvent {
    data class Clicked(val key: String) : PillButtonEvent
}
