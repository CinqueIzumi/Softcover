package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toHowItReadBlock
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Writes [BookDetailUiState.howItRead] from the reader's stored traits for the book. The section exists
 * only once the book has been started, and the traits are not observed while it is hidden.
 */
internal class HowItReadSectionCollector : BookDetailCollector {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        val traitsFlow = scope.state
            .map { it.book?.status.isStarted() }
            .distinctUntilChanged()
            .flatMapLatest { started ->
                if (started) {
                    dependencies.observeBookTraitsUseCase(bookId = scope.currentState.bookId)
                } else {
                    flowOf(null)
                }
            }

        val expandedFlow = scope.state
            .map { it.howItReadExpanded }
            .distinctUntilChanged()

        combine(traitsFlow, expandedFlow) { traits, expanded ->
            traits?.toHowItReadBlock(expanded = expanded)
        }.collectLatest { block ->
            scope.setState { state ->
                state.copy(howItRead = block)
            }
        }
    }

    private fun BookStatus?.isStarted(): Boolean = when (this) {
        BookStatus.Reading, BookStatus.Paused, BookStatus.Read, BookStatus.DidNotFinish -> true
        BookStatus.WantToRead, BookStatus.None, null -> false
    }
}
