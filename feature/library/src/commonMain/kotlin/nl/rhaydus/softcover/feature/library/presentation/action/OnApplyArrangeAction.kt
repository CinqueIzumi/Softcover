package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Commits an Arrange-sheet draft (sort mode + direction + layout, with the "show titles" toggle
 * folded into [gridLayout] per the `LibraryLayoutChip` mapping) in one shot. The UI holds all three
 * as local draft state — initialized from `state.sortModeFor(tabId)` / `sortDirectionFor(tabId)` /
 * `state.gridLayout` — and dispatches this once, on "Show N titles"; dismissing the sheet without
 * tapping it never calls this action, so nothing is persisted.
 *
 * Fires [LibraryDependencies.setLibrarySortUseCase] and [LibraryDependencies.setLibraryGridLayoutUseCase]
 * directly rather than inventing combined persistence — sort is per-tab, layout is global, so two
 * writes is the correct shape. This is the only place either use case is called now that the old
 * per-field live actions (grid layout / show-titles / sort mode) are gone — the sheet's draft
 * replaces their "dispatch on every tap" behavior with "dispatch once, on commit."
 */
internal class OnApplyArrangeAction(
    private val tabId: String,
    private val sortMode: LibrarySortMode,
    private val sortDirection: SortDirection,
    private val gridLayout: LibraryGridLayout,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        // Committing a sort change (the common reason to open the sheet) leaves rearrange mode —
        // the positional order the user was editing may no longer be what's shown. Harmless to
        // reset unconditionally when the sort didn't actually change.
        scope.setState { it.copy(isRearranging = false) }

        dependencies.setLibrarySortUseCase(
            tabId = tabId,
            mode = sortMode,
            direction = sortDirection,
        ).onFailure {
            AppLog.e("$it")
        }

        dependencies.setLibraryGridLayoutUseCase(newLayout = gridLayout).onFailure {
            AppLog.e("$it")
        }
    }
}
