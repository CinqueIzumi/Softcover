package nl.rhaydus.softcover.core.component.state

sealed interface EmptyStateEvent {
    data object ButtonClicked : EmptyStateEvent
}
