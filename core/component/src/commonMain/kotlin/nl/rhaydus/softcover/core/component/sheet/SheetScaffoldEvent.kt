package nl.rhaydus.softcover.core.component.sheet

sealed interface SheetScaffoldEvent {
    data object PrimaryClicked : SheetScaffoldEvent

    data object SecondaryClicked : SheetScaffoldEvent

    data object HeaderActionClicked : SheetScaffoldEvent

    /**
     * The sheet has finished closing; on a bottom sheet, after its slide-down has played. A host that
     * leaves the screen because of a sheet action does so here, never on the click or on a
     * [SheetScaffoldUiModel.closeRequested] write, or the screen removes the sheet mid-animation.
     */
    data object Dismissed : SheetScaffoldEvent
}
