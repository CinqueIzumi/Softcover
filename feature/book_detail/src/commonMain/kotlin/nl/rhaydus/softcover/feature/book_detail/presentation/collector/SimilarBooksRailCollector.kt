package nl.rhaydus.softcover.feature.book_detail.presentation.collector

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.toSimilarBooksRail
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

/**
 * Maps the loaded similar books to the rail, minus any the reader has shelved since: the shelved ids are
 * observed for the loaded ids, so a book shelved later drops out. The books themselves are read from
 * [BookDetailLocalVariables.similarBooks]; the state's ids are what re-runs the mapping.
 *
 * Writes nothing until the first page has settled, so the stand-ins stay up while it loads.
 */
internal class SimilarBooksRailCollector : BookDetailCollector {
    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onLaunch(
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
        dependencies: BookDetailDependencies,
    ) {
        scope.state
            .mapNotNull { state ->
                val sourceId = state.book?.id ?: return@mapNotNull null
                val loadedIds = state.similarBookIds ?: return@mapNotNull null

                SimilarBooksRailSnapshot(
                    sourceBookId = sourceId,
                    loadedIds = loadedIds,
                    isLoading = state.isLoadingSimilarBooks,
                    hasMore = state.similarBooksHasMore,
                )
            }
            .distinctUntilChanged()
            .flatMapLatest { snapshot ->
                dependencies
                    .observeShelvedBookIdsUseCase(ids = snapshot.loadedIds)
                    .map { shelvedIds -> snapshot to shelvedIds }
            }
            .collectLatest { (snapshot, shelvedIds) ->
                val books = scope.currentLocalVariables.similarBooks.filter {
                    it.id in snapshot.loadedIds && it.id !in shelvedIds
                }

                scope.setState {
                    it.copy(
                        similarBooks = books.toSimilarBooksRail(
                            sourceBookId = snapshot.sourceBookId,
                            isAppending = snapshot.isLoading && books.isNotEmpty(),
                            canLoadMore = snapshot.hasMore,
                        ),
                    )
                }
            }
    }
}
