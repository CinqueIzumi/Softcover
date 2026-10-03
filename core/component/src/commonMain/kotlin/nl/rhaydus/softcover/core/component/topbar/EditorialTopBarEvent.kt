package nl.rhaydus.softcover.core.component.topbar

/** Everything an [EditorialTopBar] reports. */
sealed interface EditorialTopBarEvent {
    /** The back button was clicked. */
    data object BackClicked : EditorialTopBarEvent
}
