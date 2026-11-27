package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toYourCopyPanel
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Writes [BookDetailUiState.yourCopy] from the reader's stored copy details and the currency catalogue.
 * The section exists for any book the reader has a copy of, whatever its status, and the copy is not
 * observed while it is hidden.
 */
internal class YourCopySectionCollector : BookDetailCollector {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        val currencies = dependencies.getCurrenciesUseCase()

        scope.state
            .map { it.book?.userBook != null }
            .distinctUntilChanged()
            .flatMapLatest { hasCopy ->
                if (hasCopy) {
                    dependencies.observeBookCopyUseCase(bookId = scope.currentState.bookId)
                } else {
                    flowOf(null)
                }
            }
            .collectLatest { copy ->
                scope.setState { state ->
                    state.copy(yourCopy = copy?.toYourCopyPanel(currencies = currencies))
                }
            }
    }
}
