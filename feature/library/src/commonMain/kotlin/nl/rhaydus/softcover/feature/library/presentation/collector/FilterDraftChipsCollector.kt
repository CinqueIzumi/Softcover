package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterDraft
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryFilterSheetSelection
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.state.TagSource
import nl.rhaydus.softcover.feature.library.presentation.state.filterSheetScaffoldFor
import nl.rhaydus.softcover.feature.library.presentation.state.withSearchText
import nl.rhaydus.toad.ActionScope

/**
 * Derives the Filter sheet's resolved facet chips (`component-contract.md` § 7.2 R9) off
 * [LibraryUiState.filterDraft] — recomputes on every draft edit (facet toggle, category
 * pick, search keystroke), and clears to `null` while the sheet is closed.
 */
internal class FilterDraftChipsCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                FilterDraftChipsSnapshot(
                    draft = state.filterDraft,
                    filterChipsByTab = state.filterChipsByTab,
                    filterOptionsByTab = state.filterOptionsByTab,
                    booksByTab = state.booksByTab,
                    editionsByTab = state.editionsByTab,
                    addedAtByTab = state.addedAtByTab,
                    bookByBookId = state.bookByBookId,
                    userTagsByBookId = state.userTagsByBookId,
                    sortModeByTab = state.sortModeByTab,
                    sortDirectionByTab = state.sortDirectionByTab,
                    searchQuery = state.searchQuery,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val selection = withContext(dependencies.defaultDispatcher) {
                    snapshot.compute()
                }

                scope.setState { state ->
                    state.copy(
                        filterSheet = state.filterSheet.copy(
                            scaffold = if (selection == null) {
                                state.filterSheet.scaffold
                            } else {
                                filterSheetScaffoldFor(
                                    resultCount = selection.resultCount,
                                    filtersActive = selection.clearAllEnabled,
                                )
                            },
                            selection = selection?.withCurrentSearch(draft = state.filterDraft),
                        ),
                    )
                }
            }
    }
}

/** A stale pass must not revert the search text an action already wrote for the caret. */
private fun LibraryFilterSheetSelection.withCurrentSearch(draft: LibraryFilterDraft?): LibraryFilterSheetSelection {
    if (draft == null) return this

    return TagSource.entries.fold(this) { selection, source ->
        selection.withSearchText(
            source = source,
            text = draft.sectionFor(source = source).search,
        )
    }
}
