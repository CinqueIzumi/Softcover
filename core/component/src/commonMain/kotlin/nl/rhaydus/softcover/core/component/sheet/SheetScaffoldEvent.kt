package nl.rhaydus.softcover.core.component.sheet

sealed interface SheetScaffoldEvent {
    data object PrimaryClicked : SheetScaffoldEvent

    data object SecondaryClicked : SheetScaffoldEvent

    data object HeaderActionClicked : SheetScaffoldEvent

    data object Dismissed : SheetScaffoldEvent
}
