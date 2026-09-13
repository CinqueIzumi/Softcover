package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.uibinding.progress.toProgressSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book and the reader's stored progress unit to the progress sheet's UI model off the
 * composition (R9), so `BookDetailShelf` only ever forwards `BookDetailUiState.progressSheet`.
 *
 * A collector rather than a patch to each action that assigns `book`: eight actions plus
 * `UserBooksFlowCollector` write that field, and a derived field one of them forgets to update is a
 * bug every gate in this repo passes.
 */
internal class ProgressSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                ProgressSheetSnapshot(
                    book = it.book,
                    selectedTab = it.selectedProgressSheetTab,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState {
                    it.copy(
                        progressSheet = snapshot.book?.toProgressSheetUiModel(selectedTab = snapshot.selectedTab),
                    )
                }
            }
    }
}
