package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.bookcard.BookCardSet
import nl.rhaydus.softcover.feature.book_detail.presentation.action.launchSimilarBooksPage
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.SIMILAR_BOOKS_STAND_IN_CARDS
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLens
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Starts the first Similar books page once the book has loaded and The Book lens is up, so the Yours lens
 * and a book still loading cost no request. Each source id loads once; the book's id is the canonical one
 * the fetch resolved to.
 */
internal class SimilarBooksLoadCollector : BookDetailCollector {
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .map {
                SimilarBooksLoadSnapshot(
                    sourceBookId = it.book?.id,
                    isTheBookLens = it.selectedLens == BookDetailLens.THE_BOOK,
                )
            }
            .distinctUntilChanged()
            .collectLatest { snapshot ->
                val sourceId = snapshot.sourceBookId ?: return@collectLatest

                if (snapshot.isTheBookLens.not() || scope.currentLocalVariables.similarBooksSourceId == sourceId) {
                    return@collectLatest
                }

                scope.currentLocalVariables.similarBooksLoadJob?.cancel()

                scope.setLocalVariables {
                    it.copy(
                        similarBooksSourceId = sourceId,
                        similarBooksOffset = 0,
                        similarBooksLoadJob = null,
                        similarBooks = emptyList(),
                    )
                }

                scope.setState {
                    it.copy(
                        similarBooks = it.similarBooks.copy(
                            set = BookCardSet.standIns(SIMILAR_BOOKS_STAND_IN_CARDS),
                            isAppending = false,
                        ),
                        similarBookIds = null,
                        isLoadingSimilarBooks = false,
                        similarBooksHasMore = true,
                    )
                }

                launchSimilarBooksPage(
                    scope = scope,
                    dependencies = dependencies,
                )
            }
    }
}
