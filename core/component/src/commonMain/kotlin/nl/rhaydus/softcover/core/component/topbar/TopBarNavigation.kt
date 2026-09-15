package nl.rhaydus.softcover.core.component.topbar

/**
 * The leading slot of a [TopBar]: either nothing, or a back affordance that reports
 * [TopBarEvent.BackClicked].
 *
 * Sealed rather than a nullable lambda, so "this screen has no way back" is a state the type admits
 * deliberately rather than an omission (R2).
 */
sealed interface TopBarNavigation {
    /** A root surface — nothing leads out of it. */
    data object None : TopBarNavigation

    /** A pushed surface: a back arrow, inked for the bar's [TopBarUiModel.surface]. */
    data object Back : TopBarNavigation
}
