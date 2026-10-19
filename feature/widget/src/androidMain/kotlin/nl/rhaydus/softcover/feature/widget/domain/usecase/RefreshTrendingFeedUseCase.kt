package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.softcover.core.book.domain.usecase.RefreshRecentBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshUpcomingBooksUseCase
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFetchTracker
import kotlin.time.Duration

internal class RefreshTrendingFeedUseCase(
    private val refreshTrendingBooks: RefreshTrendingBooksUseCase,
    private val refreshRecentBooks: RefreshRecentBooksUseCase,
    private val refreshUpcomingBooks: RefreshUpcomingBooksUseCase,
    private val fetchTracker: TrendingFetchTracker,
) {
    suspend operator fun invoke(
        feed: TrendingFeed,
        maxAge: Duration,
    ): Result<Unit> = fetchTracker.track(feed = feed) {
        when (feed) {
            is TrendingFeed.Trending -> refreshTrendingBooks(
                period = feed.period,
                maxAge = maxAge,
            )

            is TrendingFeed.Recent -> refreshRecentBooks(
                window = feed.window,
                maxAge = maxAge,
            )

            is TrendingFeed.Upcoming -> refreshUpcomingBooks(
                window = feed.window,
                maxAge = maxAge,
            )
        }
    }
}
