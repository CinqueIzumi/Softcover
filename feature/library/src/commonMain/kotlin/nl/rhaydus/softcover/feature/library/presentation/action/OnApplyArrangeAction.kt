package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Commits [LibraryUiState.arrangeDraft] (sort mode + direction + layout, with the "show titles"
 * toggle folded into `gridLayout` per the `LibraryLayoutChip` mapping) in one shot, on "Show N
 * titles"; dismissing the sheet without tapping it never calls this action, so nothing is
 * persisted. A no-op if the draft is already gone (the sheet closed between the tap and this
 * action running).
 */
internal class OnApplyArrangeAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val draft = scope.currentState.arrangeDraft ?: return

        // Committing a sort change (the common reason to open the sheet) leaves rearrange mode —
        // the positional order the user was editing may no longer be what's shown. Harmless to
        // reset unconditionally when the sort didn't actually change.
        scope.setState { it.copy(isRearranging = false) }

        dependencies.setLibrarySortUseCase(
            tabId = draft.tabId,
            mode = draft.sortMode,
            direction = draft.sortDirection,
        ).onFailure {
            AppLog.e("$it")
        }

        dependencies.setLibraryGridLayoutUseCase(newLayout = draft.gridLayout).onFailure {
            AppLog.e("$it")
        }
    }
}
