package nl.rhaydus.softcover.core.component.topbar

/** Everything a [TopBar] reports (R1). */
sealed interface TopBarEvent {
    /** The back button was tapped. */
    data object BackClicked : TopBarEvent
}
