package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryArrangeDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Opens or closes the Arrange sheet. Opening seeds [LibraryUiState.arrangeDraft] off the currently
 * selected tab's committed sort/layout so the sheet always starts in sync; closing (scrim, back, or
 * after [OnApplyArrangeAction] commits) clears it, discarding anything the sheet's chips edited.
 */
internal class OnArrangeSheetExpandedChangeAction(
    private val expanded: Boolean,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { state ->
            state.copy(
                isArrangeSheetExpanded = expanded,
                arrangeDraft = if (expanded) {
                    LibraryArrangeDraft(
                        tabId = state.selectedTabId,
                        sortMode = state.sortModeFor(tabId = state.selectedTabId),
                        sortDirection = state.sortDirectionFor(tabId = state.selectedTabId),
                        gridLayout = state.gridLayout,
                    )
                } else {
                    null
                },
            )
        }
    }
}
