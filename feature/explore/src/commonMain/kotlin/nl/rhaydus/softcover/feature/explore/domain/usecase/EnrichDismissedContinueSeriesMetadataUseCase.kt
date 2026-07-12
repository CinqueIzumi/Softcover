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
 * Backfills display metadata for hidden "up next in your series" rows persisted before schema v44,
 * which only carry a bare id. Runs lazily when the Hidden Suggestions screen loads (not at app
 * startup): resolves each row **independently** via [runCatchingLogged] so one failure never blocks
 * the rest, and writes results back through the same dismiss path that already persists metadata at
 * dismiss time. Idempotent - a row that already carries metadata is skipped, so a going-forward
 * dismissal (already enriched) costs nothing here.
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
            .filter { it.title == null }

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
                bookId = book.bookId,
                title = fetched.title,
                coverUrl = fetched.coverUrl,
                authorText = fetched.authorString,
                seriesName = fetched.bookSeries?.name,
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
