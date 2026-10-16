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
import nl.rhaydus.softcover.feature.book_detail.presentation.state.deleteQuoteSheetScaffold
import nl.rhaydus.toad.ActionScope

/** The only writer of [BookDetailUiState.quoteDeleteSheet]: open while a delete is awaiting its confirm. */
internal class QuoteDeleteSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.quoteDeleteRequestId != null }
            .distinctUntilChanged()
            .collectLatest { requested ->
                scope.setState {
                    it.copy(
                        quoteDeleteSheet = if (requested && it.quoteDeleteRequestId != null) {
                            it.quoteDeleteSheet.openOrUpdate(model = deleteQuoteSheetScaffold())
                        } else {
                            it.quoteDeleteSheet.requestClose()
                        },
                    )
                }
            }
    }
}
