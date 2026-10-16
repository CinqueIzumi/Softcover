package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toQuotesSection
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Keeps [BookDetailUiState.quotesSection] in step with the reader's stored quotes for the book. The
 * section exists only while the book is shelved, and a quote saved offline shows here before it syncs.
 */
internal class QuotesSectionCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.book?.userBook != null }
            .distinctUntilChanged()
            .collectLatest { shelved ->
                if (shelved.not()) {
                    scope.setState { it.copy(quotesSection = null) }

                    return@collectLatest
                }

                dependencies.observeBookQuotesUseCase(bookId = scope.currentState.bookId)
                    .map { it.toQuotesSection() }
                    .distinctUntilChanged()
                    .collect { section ->
                        scope.setState { it.copy(quotesSection = section) }
                    }
            }
    }
}
