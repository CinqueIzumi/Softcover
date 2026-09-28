package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import nl.rhaydus.softcover.core.component.selection.SelectionMarkUiModel
import nl.rhaydus.softcover.core.component.selection.SelectionMarkVariant
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps [LibraryUiState.selectionMode] and [LibraryUiState.selectedBookIds] to a
 * [SelectionMarkUiModel] per book off the composition (`component-contract.md` § 7.2 R9), keyed
 * by [Book.id] into [LibraryUiState.selectionMarks]. Empty outside selection mode. The variant
 * follows [LibraryUiState.gridLayout]: [LibraryGridLayout.LIST_COMPACT] renders a leading row mark,
 * every other layout an on-cover mark.
 */
internal class SelectionMarksCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state ->
                SelectionMarksSnapshot(
                    selectionMode = state.selectionMode,
                    selectedBookIds = state.selectedBookIds,
                    booksByTab = state.booksByTab,
                    gridLayout = state.gridLayout,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val selectionMarks = withContext(dependencies.defaultDispatcher) {
                    if (snapshot.selectionMode.not()) {
                        persistentMapOf()
                    } else {
                        val variant = if (snapshot.gridLayout == LibraryGridLayout.LIST_COMPACT) {
                            SelectionMarkVariant.RowLeading
                        } else {
                            SelectionMarkVariant.OnCover
                        }

                        snapshot.booksByTab.values.asSequence()
                            .flatten()
                            .associate { book ->
                                book.id to SelectionMarkUiModel(
                                    selected = book.id in snapshot.selectedBookIds,
                                    variant = variant,
                                )
                            }
                            .toImmutableMap()
                    }
                }

                scope.setState {
                    it.copy(selectionMarks = selectionMarks)
                }
            }
    }
}
