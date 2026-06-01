package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookStatus
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.explore.domain.model.SeriesContinuationSeed
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetContinueSeriesBooksUseCase(
    private val booksRepository: BooksRepository,
    private val exploreRepository: ExploreRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<List<Book>> = combine(
        booksRepository.books,
        exploreRepository.dismissedContinueSeriesBookIds,
        exploreRepository.dismissedContinueSeriesIds,
    ) { books, dismissedBookIds, dismissedSeriesIds ->
        deriveSeeds(
            books = books,
            dismissedSeriesIds = dismissedSeriesIds.toSet(),
        ) to dismissedBookIds.toSet()
    }
        .distinctUntilChanged()
        .mapLatest { (seeds, dismissedBookIds) ->
            fetchNextBooks(seeds = seeds)
                .filter { it.id !in dismissedBookIds }
        }

    private fun deriveSeeds(
        books: List<Book>,
        dismissedSeriesIds: Set<Int>,
    ): List<SeriesContinuationSeed> {
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

                if (maxRead >= series.amountOfBooks) return@mapNotNull null

                SeriesContinuationSeed(
                    seriesId = seriesId,
                    afterPosition = maxRead,
                )
            }
    }

    private suspend fun fetchNextBooks(seeds: List<SeriesContinuationSeed>): List<Book> = coroutineScope {
        seeds
            .map { seed ->
                async {
                    runCatching {
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
