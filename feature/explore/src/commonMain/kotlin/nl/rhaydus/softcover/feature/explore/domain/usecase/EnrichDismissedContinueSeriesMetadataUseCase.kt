package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

/**
 * Backfills the metadata a hidden "up next in your series" row is missing when it predates the
 * schema that introduced it: display fields (title, cover, author, series name) before v44, and the
 * series cursor (series id + position) before v45. The cursor matters beyond presentation - a row
 * without it cannot move the series forward, so the series stays stuck on the hidden book.
 *
 * Runs lazily when the Explore shelf or the Hidden Suggestions screen loads (not at app startup):
 * resolves each row **independently** via [runCatchingLogged] so one failure never blocks the rest,
 * and writes results back through the same dismiss path that already persists metadata at dismiss
 * time. Idempotent - a row that already carries both is skipped, so a going-forward dismissal
 * (written complete) costs nothing here.
 *
 * Returns [Unit] rather than the usual `Result<T>`: with per-row isolation there is no single
 * aggregate outcome to report, and any row that fails to resolve is simply retried on the next open.
 */
class EnrichDismissedContinueSeriesMetadataUseCase(
    private val exploreRepository: ExploreRepository,
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke() {
        val booksNeedingEnrichment = exploreRepository.dismissedContinueSeriesBooks.firstOrNull().orEmpty()
            .filter { it.title == null || it.seriesPosition == null }

        val seriesNeedingEnrichment = exploreRepository.dismissedContinueSeries.firstOrNull().orEmpty()
            .filter { it.seriesName == null }

        if (booksNeedingEnrichment.isEmpty() && seriesNeedingEnrichment.isEmpty()) return

        val localBooks = if (seriesNeedingEnrichment.isNotEmpty()) {
            booksRepository.books.firstOrNull().orEmpty()
        } else {
            emptyList()
        }

        coroutineScope {
            booksNeedingEnrichment
                .map { book -> async { enrichBook(book = book) } }
                .plus(
                    seriesNeedingEnrichment.map { series ->
                        async {
                            enrichSeries(
                                series = series,
                                localBooks = localBooks,
                            )
                        }
                    },
                )
                .awaitAll()
        }
    }

    private suspend fun enrichBook(book: DismissedSeriesBook) {
        runCatchingLogged {
            val fetched = booksRepository.fetchBookById(id = book.bookId)

            exploreRepository.dismissContinueSeriesBook(
                book = DismissedSeriesBook(
                    bookId = book.bookId,
                    title = fetched.title,
                    coverUrl = fetched.coverUrl,
                    authorText = fetched.authorString,
                    seriesName = fetched.bookSeries?.name,
                    seriesId = fetched.bookSeries?.id,
                    // The cursor must clear the book's *last* position: an omnibus spanning 1-3 that
                    // only advanced to 1 would match the same book again on the next fetch.
                    seriesPosition = fetched.positionsInSeries.lastOrNull(),
                ),
            )
        }
    }

    private suspend fun enrichSeries(
        series: DismissedSeries,
        localBooks: List<Book>,
    ) {
        runCatchingLogged {
            val seed = localBooks.firstOrNull { it.bookSeries?.id == series.seriesId }
                ?: exploreRepository.fetchNextInSeries(
                    seriesId = series.seriesId,
                    afterPosition = 0.0,
                )
                ?: return@runCatchingLogged

            // A hidden series is identified by its series name, never a single book title. If the
            // seed somehow lacks series info, leave the row on its fallback for the next pass rather
            // than mislabel it with a book title.
            val seriesName = seed.bookSeries?.name ?: return@runCatchingLogged

            exploreRepository.dismissContinueSeries(
                seriesId = series.seriesId,
                seriesName = seriesName,
                coverUrl = seed.coverUrl,
            )
        }
    }
}
