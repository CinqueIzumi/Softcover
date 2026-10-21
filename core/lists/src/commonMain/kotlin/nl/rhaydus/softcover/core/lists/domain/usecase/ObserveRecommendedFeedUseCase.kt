package nl.rhaydus.softcover.core.lists.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveSystemVibeFetchedAtUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshSystemVibeBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.SystemVibe
import nl.rhaydus.softcover.core.lists.domain.model.RecommendedFeed
import kotlin.time.Duration.Companion.hours

/**
 * Collecting the feed also refreshes both vibes from the network when their cache is over [MAX_AGE] old.
 * Recommendations win; Top Picks fill in only when Recommendations leave nothing after the owned filter.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ObserveRecommendedFeedUseCase(
    private val getRecommendedBooks: GetRecommendedBooksUseCase,
    private val observeSystemVibeFetchedAt: ObserveSystemVibeFetchedAtUseCase,
    private val refreshSystemVibeBooks: RefreshSystemVibeBooksUseCase,
) {
    operator fun invoke(limit: Int): Flow<RecommendedFeed> = combine(
        observeVibe(
            vibe = SystemVibe.Recommendations,
            limit = limit,
        ),
        observeVibe(
            vibe = SystemVibe.TopPicks,
            limit = limit,
        ),
    ) { recommended, top ->
        val recommendationsSettledEmpty = recommended.books.isEmpty() && recommended.pending.not()

        val source = if (recommendationsSettledEmpty && top.books.isNotEmpty()) {
            SystemVibe.TopPicks
        } else {
            SystemVibe.Recommendations
        }
        val books = if (source == SystemVibe.TopPicks) top.books else recommended.books

        RecommendedFeed(
            books = books,
            isLoading = books.isEmpty() && (recommended.pending || top.pending),
            source = source,
        )
    }.distinctUntilChanged()

    /**
     * Pending only while a vibe that had no cache when collection started is still refreshing. The books are
     * re-queried once the refresh settles, so the stored ranking and its fetch time never surface apart.
     */
    private fun observeVibe(
        vibe: SystemVibe,
        limit: Int,
    ): Flow<VibeState> = channelFlow {
        val hadCache = observeSystemVibeFetchedAt(vibe = vibe).firstOrNull() != null
        val refreshSettled = MutableStateFlow(value = false)

        launch {
            refreshSystemVibeBooks(
                vibe = vibe,
                maxAge = MAX_AGE,
            ).onFailure { error ->
                AppLog.e(
                    error,
                    "Failed to fetch $vibe books",
                )
            }

            refreshSettled.value = true
        }

        refreshSettled
            .flatMapLatest { settled ->
                getRecommendedBooks(
                    vibe = vibe,
                    limit = limit,
                ).map { books ->
                    VibeState(
                        books = books,
                        pending = hadCache.not() && settled.not(),
                    )
                }
            }
            .collect { state ->
                send(state)
            }
    }

    private data class VibeState(
        val books: List<Book>,
        val pending: Boolean,
    )

    private companion object {
        val MAX_AGE = 1.hours
    }
}
