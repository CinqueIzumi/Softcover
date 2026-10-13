package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
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

// Seeds are deliberately uncapped: every seed resolves in one batched request, so a failure empties
// the whole shelf rather than one card.

class GetContinueSeriesBooksUseCase(
    private val booksRepository: BooksRepository,
    private val exploreRepository: ExploreRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Book>> {
        val dismissedBooks = exploreRepository.dismissedContinueSeriesBooks
        val dismissedSeriesIds = exploreRepository.dismissedContinueSeriesIds

        val nextBooks = combine(
            booksRepository.books,
            dismissedBooks,
            dismissedSeriesIds,
        ) { books, dismissed, dismissedSeries ->
            deriveSeeds(
                books = books,
                dismissedBooks = dismissed,
                dismissedSeriesIds = dismissedSeries.toSet(),
            )
        }
            .distinctUntilChanged()
            .mapLatest { seeds -> fetchNextBooks(seeds = seeds) }

        return combine(
            nextBooks,
            dismissedBooks,
            dismissedSeriesIds,
        ) { books, dismissed, dismissedSeries ->
            val dismissedBookIds = dismissed.map { it.bookId }.toSet()
            val dismissedSeriesIdSet = dismissedSeries.toSet()

            books.filter { book ->
                book.id !in dismissedBookIds && book.bookSeries?.id !in dismissedSeriesIdSet
            }
        }
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
            .filter { it.positionsInSeries.isNotEmpty() }
            .filter {
                it.userBook?.status == BookStatus.Reading ||
                    it.userBook?.status == BookStatus.Read
            }
            .mapNotNull { book -> book.bookSeries?.let { series -> book to series } }
            .filter { (_, series) -> series.id !in seriesIdsWithDnf }
            .filter { (_, series) -> series.id !in dismissedSeriesIds }
            .groupBy { (_, series) -> series.id }
            .mapNotNull { (seriesId, group) ->
                val maxRead = group.maxOf { (book, _) -> book.positionsInSeries.last() }
                val (_, series) = group.first()

                // The cursor is the furthest point in the series the user has settled: read through,
                // or explicitly hidden. Hiding a suggestion has to move it, otherwise the next-in-
                // series query (`position > cursor`, `limit 1`) keeps returning the hidden book and
                // the series suggests nothing ever again.
                val cursor = maxOf(
                    maxRead,
                    dismissedFloors[seriesId] ?: maxRead,
                )

                if (cursor >= series.amountOfBooks) return@mapNotNull null

                // A series with no read book yet (Reading-only, never finished) sorts last via the
                // empty fallback.
                val mostRecentReadDate = group
                    .mapNotNull { (book, _) -> book.userBook?.lastReadDate }
                    .maxOrNull()
                    .orEmpty()

                mostRecentReadDate to SeriesContinuationSeed(
                    seriesId = seriesId,
                    afterPosition = cursor,
                )
            }
            .sortedByDescending { (mostRecentReadDate, _) -> mostRecentReadDate }
            .map { (_, seed) -> seed }
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

    private suspend fun fetchNextBooks(seeds: List<SeriesContinuationSeed>): List<Book> =
        runCatchingLogged {
            exploreRepository.fetchNextBooksInSeries(seeds = seeds)
        }.getOrNull().orEmpty()
}
