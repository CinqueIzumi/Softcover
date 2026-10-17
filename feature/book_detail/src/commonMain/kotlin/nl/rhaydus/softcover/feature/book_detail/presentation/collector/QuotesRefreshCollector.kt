package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Pulls the book's quotes from Hardcover once, the first time the reader looks at the Yours lens of a
 * shelved book. [QuotesSectionCollector] shows what the pull stores; a failed pull leaves the stored quotes.
 */
internal class QuotesRefreshCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.book?.userBook != null && it.selectedLens == BookDetailLens.YOURS }
            .distinctUntilChanged()
            .filter { it }
            .take(1)
            .collect {
                dependencies.refreshBookQuotesUseCase(bookId = scope.currentState.bookId)
                    .onFailure { error ->
                        AppLog.e("Failed to refresh quotes: $error")
                    }
            }
    }
}
