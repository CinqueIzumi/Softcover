package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toInProgress
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toInProgressSectionHeader
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the book's reading progress to the "In progress" section's UI model off the composition,
 * so `InProgressSection` only ever renders `BookDetailUiState.inProgress`.
 */
internal class InProgressCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.book }
            .distinctUntilChanged()
            .collectLatest { book ->
                scope.setState {
                    it.copy(
                        inProgress = book?.toInProgress(),
                        inProgressSectionHeader = book?.toInProgressSectionHeader()
                            ?: it.inProgressSectionHeader,
                    )
                }
            }
    }
}
