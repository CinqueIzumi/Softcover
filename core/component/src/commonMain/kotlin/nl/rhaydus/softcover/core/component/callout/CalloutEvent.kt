package nl.rhaydus.softcover.core.component.callout

sealed interface CalloutEvent {
    data object ActionClicked : CalloutEvent

    data object DismissClicked : CalloutEvent
}
