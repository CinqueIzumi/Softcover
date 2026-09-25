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

// Seeds are deliberately uncapped: the shelf shows every series the reader is mid-stream on.
//
// This was briefly trimmed to the 8 most recently read while the API's rate limit had no
// client-side handling, because `fetchNextBooks` used to issue one `GetNextBookInSeries` request
// per seed concurrently, and a series reader would spend the whole burst bucket - and roughly one
// second of loading per seed under `RateLimitInterceptor`'s pacing - on this one shelf. See
// the Network Layer section of `docs/reference/architecture.md` for what a request costs.
//
// `fetchNextBooks` now resolves every seed through `ExploreRepository.fetchNextBooksInSeries` in
// a single batched request (`GetNextBooksInSeries` + one `GetBooksByIdsQuery`), regardless of seed
// count, so neither the burst-bucket cost nor the per-seed latency applies any more. The trade is
// error isolation: the old fan-out wrapped each seed in its own `runCatchingLogged`, so one failing
// series never affected the rest; a single batched request now succeeds or fails as a whole, so a
// failure costs the entire shelf rather than one card. That is an acceptable trade for 2 requests
// instead of N - the rate limiter and the read retry both sit underneath it - but it is a real
// behavior change, not an oversight.

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

                // `lastReadDate` (set on finish/progress) is the most defensible recency signal the
                // domain model exposes for "which series is this user mid-stream on right now" -
                // it is what LibraryStats/SortSql already use for the same purpose. A series with no
                // read book yet (Reading-only, never finished) sorts last via the empty fallback.
                val mostRecentReadDate = group
                    .mapNotNull { it.userBook?.lastReadDate }
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
