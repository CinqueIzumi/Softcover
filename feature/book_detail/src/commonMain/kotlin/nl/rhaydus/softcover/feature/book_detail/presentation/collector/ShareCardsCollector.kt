package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book, the currently-displayed edition, the reader's identity and the chosen variant to the
 * share sheet.
 *
 * The reading-update card is offered only when the reader has something personal to share; without it
 * the book card is the only variant and no toggle is shown.
 */
internal class ShareCardsCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.shareCardsSnapshot() }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                scope.setState { state ->
                    val model = snapshot.toShareSheetUiModel(progress = state.shareCapture.progress)

                    if (model == null || state.shareSheetRequested.not()) {
                        state.copy(shareSheet = state.shareSheet.requestClose())
                    } else {
                        state.copy(shareSheet = state.shareSheet.openOrUpdate(model = model))
                    }
                }
            }
    }
}
