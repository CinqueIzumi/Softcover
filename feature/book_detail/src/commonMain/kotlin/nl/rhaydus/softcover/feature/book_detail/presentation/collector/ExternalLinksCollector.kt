package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toExternalLinksUiModel
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * The only writer of [BookDetailUiState.externalLinks]: the "Find it" pills and their URLs, derived from
 * [BookDetailUiState.displayedEdition], which several collectors and actions move.
 */
internal class ExternalLinksCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map { it.displayedEdition?.let { edition -> edition.isbn13 to edition.isbn10 } }
            .distinctUntilChanged()
            .collectLatest {
                scope.setState { state ->
                    state.copy(externalLinks = state.displayedEdition.toExternalLinksUiModel())
                }
            }
    }
}
