package nl.rhaydus.softcover.core.component.topbar

/** Everything a [TopBar] reports (R1). */
sealed interface TopBarEvent {
    /** The back affordance was tapped. Only ever raised when [TopBarNavigation.Back] is in the model. */
    data object BackClicked : TopBarEvent
}
