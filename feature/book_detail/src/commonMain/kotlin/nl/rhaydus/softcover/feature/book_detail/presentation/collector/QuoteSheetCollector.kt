package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.sheet.openOrUpdate
import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toQuoteSheetUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book and the quote draft to the quote sheet's UI model off the composition. The only writer
 * of [BookDetailUiState.quoteSheet]: it opens while a draft exists and asks the sheet to hide otherwise.
 */
internal class QuoteSheetCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                QuoteSheetSnapshot(
                    book = it.book,
                    draft = it.quoteDraft,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val book = snapshot.book
                val draft = snapshot.draft

                scope.setState {
                    it.copy(
                        quoteSheet = if (book == null || draft == null || it.quoteDraft == null) {
                            it.quoteSheet.requestClose()
                        } else {
                            it.quoteSheet.openOrUpdate(
                                model = book.toQuoteSheetUiModel(draft = draft),
                            )
                        },
                    )
                }
            }
    }
}
