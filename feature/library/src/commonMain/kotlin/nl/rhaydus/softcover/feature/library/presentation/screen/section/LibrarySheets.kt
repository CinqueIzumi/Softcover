package nl.rhaydus.softcover.feature.library.presentation.screen.section

import androidx.compose.runtime.Composable
import nl.rhaydus.softcover.core.component.lists.ChooseListsBottomSheet
import nl.rhaydus.softcover.core.component.lists.ChooseListsEvent
import nl.rhaydus.softcover.core.component.sheet.SheetScaffold
import nl.rhaydus.softcover.core.component.sheet.SheetScaffoldEvent
import nl.rhaydus.softcover.core.component.sheet.modelOrNull
import nl.rhaydus.softcover.feature.library.presentation.action.LibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnApplyFiltersAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnArrangeSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveFromLibraryAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkRemoveSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnBulkToggleListMembershipAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnChooseListsSheetDismissedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnCreateListRequestedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterDraftChipToggledAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterDraftClearAllAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterDraftTagSearchChangedAction
import nl.rhaydus.softcover.feature.library.presentation.action.OnFilterSheetExpandedChangeAction
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryArrangeSheet
import nl.rhaydus.softcover.feature.library.presentation.component.LibraryFilterSheet
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterSheetEvent
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

@Composable
internal fun LibrarySheets(
    state: LibraryUiState,
    currentTabId: String?,
    runAction: (LibraryAction) -> Unit,
) {
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
            model = state.filterSheet,
            onEvent = { event ->
                when (event) {
                    LibraryFilterSheetEvent.ApplyClicked -> runAction(OnApplyFiltersAction())

                    LibraryFilterSheetEvent.ClearAllClicked -> runAction(OnFilterDraftClearAllAction())

                    LibraryFilterSheetEvent.Dismissed -> runAction(OnFilterSheetExpandedChangeAction(expanded = false))

                    is LibraryFilterSheetEvent.ChipToggled -> runAction(OnFilterDraftChipToggledAction(key = event.key))

                    is LibraryFilterSheetEvent.TagSearchChanged -> runAction(
                        OnFilterDraftTagSearchChangedAction(query = event.query),
                    )
                }
            },
        )
    }

    state.bulkRemoveSheet.modelOrNull?.let { scaffold ->
        SheetScaffold(
            model = scaffold,
            onEvent = { event ->
                when (event) {
                    SheetScaffoldEvent.PrimaryClicked -> {
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

    state.chooseListsSheet.modelOrNull?.let { chooseListsSheet ->
        ChooseListsBottomSheet(
            model = chooseListsSheet,
            onEvent = { event ->
                when (event) {
                    is ChooseListsEvent.MembershipToggled -> {
                        runAction(
                            OnBulkToggleListMembershipAction(
                                listId = event.listId,
                                currentMembership = event.membership,
                            ),
                        )
                    }

                    ChooseListsEvent.NewListRequested -> runAction(OnCreateListRequestedAction())

                    ChooseListsEvent.Dismissed -> runAction(OnChooseListsSheetDismissedAction())
                }
            },
        )
    }
}
