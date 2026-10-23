package nl.rhaydus.softcover.feature.book_detail.presentation.action

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

private const val MAX_REQUESTS_PER_LOAD = 3

/**
 * A page that filtering empties out while the ranking has more is retried inside the same load, up to
 * [MAX_REQUESTS_PER_LOAD] requests in all, so the rail neither collapses nor waits on a scroll that cannot happen
 * with no cards. A failed page ends the paging for this screen: the rail would otherwise re-request on every
 * frame it stays near its end.
 */
internal fun launchSimilarBooksPage(
    scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    dependencies: BookDetailDependencies,
) {
    val locals = scope.currentLocalVariables
    val sourceId = locals.similarBooksSourceId ?: return

    if (locals.similarBooksLoadJob?.isActive == true || scope.currentState.similarBooksHasMore.not()) return

    scope.setState { it.copy(isLoadingSimilarBooks = true) }

    val job = dependencies.launch {
        var hasMore = true
        var requests = 0

        do {
            val page = dependencies
                .loadSimilarBooksPageUseCase(
                    sourceBookId = sourceId,
                    offset = scope.currentLocalVariables.similarBooksOffset,
                )
                .onApiFailure()
                .getOrNull()

            if (page == null) {
                hasMore = false
                break
            }

            val before = scope.currentLocalVariables.similarBooks.size

            scope.setLocalVariables { current ->
                val knownIds = current.similarBooks.map { it.canonicalId ?: it.id }.toSet()

                current.copy(
                    similarBooks = current.similarBooks + page.books.filter { (it.canonicalId ?: it.id) !in knownIds },
                    similarBooksOffset = page.nextOffset,
                )
            }

            hasMore = page.hasMore
            requests++

            val grew = scope.currentLocalVariables.similarBooks.size > before
        } while (grew.not() && hasMore && requests < MAX_REQUESTS_PER_LOAD)

        val loadedIds = scope.currentLocalVariables.similarBooks.map { it.id }.toImmutableList()

        scope.setState {
            it.copy(
                similarBookIds = loadedIds,
                similarBooksHasMore = hasMore,
                isLoadingSimilarBooks = false,
            )
        }
    }

    scope.setLocalVariables { it.copy(similarBooksLoadJob = job) }
}
