package nl.rhaydus.softcover.core.lists.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveRecommendedFetchedAtUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshRecommendedBooksUseCase
import nl.rhaydus.softcover.core.lists.domain.model.RecommendedFeed
import kotlin.time.Duration.Companion.hours

/** Collecting the feed also refreshes the recommendations from the network when the cache is over [MAX_AGE] old. */
class ObserveRecommendedFeedUseCase(
    private val getRecommendedBooks: GetRecommendedBooksUseCase,
    private val observeRecommendedFetchedAt: ObserveRecommendedFetchedAtUseCase,
    private val refreshRecommendedBooks: RefreshRecommendedBooksUseCase,
) {
    operator fun invoke(limit: Int): Flow<RecommendedFeed> = channelFlow {
        val refreshSettled = MutableStateFlow(value = false)

        launch {
            refreshRecommendedBooks(maxAge = MAX_AGE)
                .onFailure { error ->
                    AppLog.e(
                        error,
                        "Failed to fetch recommended books",
                    )
                }

            refreshSettled.value = true
        }

        combine(
            getRecommendedBooks(limit = limit),
            observeRecommendedFetchedAt(),
            refreshSettled,
        ) { books, fetchedAt, settled ->
            RecommendedFeed(
                books = books,
                isLoading = fetchedAt == null && settled.not(),
            )
        }.collect { feed ->
            send(feed)
        }
    }

    private companion object {
        val MAX_AGE = 1.hours
    }
}
