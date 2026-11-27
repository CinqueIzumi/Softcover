package nl.rhaydus.softcover.core.component.field

sealed interface SelectFieldEvent {
    data class Clicked(val key: String) : SelectFieldEvent
}
