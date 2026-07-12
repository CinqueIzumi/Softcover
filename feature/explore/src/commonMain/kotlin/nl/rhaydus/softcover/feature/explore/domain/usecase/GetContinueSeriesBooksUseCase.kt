package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.model.SeriesContinuationSeed
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetContinueSeriesBooksUseCase(
    private val booksRepository: BooksRepository,
    private val exploreRepository: ExploreRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Book>> = combine(
        booksRepository.books,
        exploreRepository.dismissedContinueSeriesBooks,
        exploreRepository.dismissedContinueSeriesIds,
    ) { books, dismissedBooks, dismissedSeriesIds ->
        deriveSeeds(
            books = books,
            dismissedBooks = dismissedBooks,
            dismissedSeriesIds = dismissedSeriesIds.toSet(),
        ) to dismissedBooks.map { it.bookId }.toSet()
    }
        .distinctUntilChanged()
        .mapLatest { (seeds, dismissedBookIds) ->
            fetchNextBooks(seeds = seeds)
                .filter { it.id !in dismissedBookIds }
        }

    private fun deriveSeeds(
        books: List<Book>,
        dismissedBooks: List<DismissedSeriesBook>,
        dismissedSeriesIds: Set<Int>,
    ): List<SeriesContinuationSeed> {
        val dismissedFloors = dismissedFloorsBySeries(dismissedBooks = dismissedBooks)

        val seriesIdsWithDnf = books
            .asSequence()
            .filter { it.userBook?.status == BookStatus.DidNotFinish }
            .mapNotNull { it.bookSeries?.id }
            .toSet()

        return books
            .asSequence()
            .filter { it.bookSeries != null && it.positionsInSeries.isNotEmpty() }
            .filter {
                it.userBook?.status == BookStatus.Reading ||
                    it.userBook?.status == BookStatus.Read
            }
            .filter { it.bookSeries!!.id !in seriesIdsWithDnf }
            .filter { it.bookSeries!!.id !in dismissedSeriesIds }
            .groupBy { it.bookSeries!!.id }
            .mapNotNull { (seriesId, group) ->
                val maxRead = group.maxOf { it.positionsInSeries.last() }
                val series = group.first().bookSeries!!

                // The cursor is the furthest point in the series the user has settled: read through,
                // or explicitly hidden. Hiding a suggestion has to move it, otherwise the next-in-
                // series query (`position > cursor`, `limit 1`) keeps returning the hidden book and
                // the series suggests nothing ever again.
                val cursor = maxOf(
                    maxRead,
                    dismissedFloors[seriesId] ?: maxRead,
                )

                if (cursor >= series.amountOfBooks) return@mapNotNull null

                SeriesContinuationSeed(
                    seriesId = seriesId,
                    afterPosition = cursor,
                )
            }
    }

    /**
     * The highest position hidden per series. Rows dismissed before schema v45 carry no position and
     * cannot move the cursor; they stay covered by the id filter until the metadata backfill fills
     * them in.
     */
    private fun dismissedFloorsBySeries(dismissedBooks: List<DismissedSeriesBook>): Map<Int, Double> = dismissedBooks
        .mapNotNull { book ->
            val seriesId = book.seriesId ?: return@mapNotNull null
            val position = book.seriesPosition ?: return@mapNotNull null

            seriesId to position
        }
        .groupBy(
            keySelector = { it.first },
            valueTransform = { it.second },
        )
        .mapValues { (_, positions) -> positions.max() }

    private suspend fun fetchNextBooks(seeds: List<SeriesContinuationSeed>): List<Book> = coroutineScope {
        seeds
            .map { seed ->
                async {
                    runCatchingLogged {
                        exploreRepository.fetchNextInSeries(
                            seriesId = seed.seriesId,
                            afterPosition = seed.afterPosition,
                        )
                    }.getOrNull()
                }
            }
            .awaitAll()
            .filterNotNull()
    }
}
