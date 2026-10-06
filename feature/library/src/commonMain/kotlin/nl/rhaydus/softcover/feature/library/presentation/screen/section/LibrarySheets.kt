package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.designsystem.haptics.LocalHaptics
import nl.rhaydus.softcover.core.component.lists.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.component.lists.ChooseListsEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkAddToListSheetShownAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveFromLibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkToggleListMembershipAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryArrangeSheet
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterSheet
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

@Composable
internal fun LibrarySheets(
    state: LibraryUiState,
    currentTabId: String?,
    runAction: (LibraryAction) -> Unit,
    onCreateNewListClick: () -> Unit,
) {
    val haptics = LocalHaptics.current

    if (state.isArrangeSheetExpanded && currentTabId != null) {
        LibraryArrangeSheet(
            state = state,
            runAction = runAction,
            onDismissRequest = {
                runAction(OnArrangeSheetExpandedChangeAction(expanded = false))
            },
        )
    }

    if (state.isFilterSheetExpanded && currentTabId != null) {
        LibraryFilterSheet(
            tabId = currentTabId,
            state = state,
            runAction = runAction,
            onDismissRequest = {
                runAction(OnFilterSheetExpandedChangeAction(expanded = false))
            },
        )
    }

    if (state.isBulkRemoveSheetShown) {
        SheetScaffold(
            model = state.bulkRemoveSheet,
            onEvent = { event ->
                when (event) {
                    SheetScaffoldEvent.PrimaryClicked -> {
                        haptics.commit()

                        runAction(OnBulkRemoveFromLibraryAction())
                    }

                    SheetScaffoldEvent.Dismissed -> runAction(OnBulkRemoveSheetExpandedChangeAction(shown = false))

                    SheetScaffoldEvent.SecondaryClicked,
                    SheetScaffoldEvent.HeaderActionClicked -> Unit
                }
            },
            content = {},
        )
    }

    val chooseListsSheet = state.chooseListsSheet

    if (state.isBulkAddToListSheetShown && state.selectedBookIds.isNotEmpty() && chooseListsSheet != null) {
        ChooseListsBottomSheet(
            model = chooseListsSheet,
            onEvent = { event ->
                when (event) {
                    is ChooseListsEvent.MembershipToggled -> {
                        haptics.commit()

                        runAction(
                            OnBulkToggleListMembershipAction(
                                listId = event.listId,
                                currentMembership = event.membership,
                            ),
                        )
                    }

                    ChooseListsEvent.NewListRequested -> onCreateNewListClick()

                    ChooseListsEvent.Dismissed -> runAction(OnBulkAddToListSheetShownAction(shown = false))
                }
            },
        )
    }
}
