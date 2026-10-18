package nl.rhaydus.softcover.feature.widget.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingFetchedAtUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveUpcomingFetchedAtUseCase
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeedFetchStatus
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFetchTracker

/** The [TrendingFeedFetchStatus] of [feed]; only [TrendingFeedFetchStatus.Settled] makes an empty ranking real. */
internal class ObserveTrendingFeedFetchingUseCase(
    private val observeTrendingFetchedAt: ObserveTrendingFetchedAtUseCase,
    private val observeUpcomingFetchedAt: ObserveUpcomingFetchedAtUseCase,
    private val fetchTracker: TrendingFetchTracker,
) {
    operator fun invoke(feed: TrendingFeed): Flow<TrendingFeedFetchStatus> {
        val fetchedAt = when (feed) {
            is TrendingFeed.Trending -> observeTrendingFetchedAt(period = feed.period)
            is TrendingFeed.Upcoming -> observeUpcomingFetchedAt(window = feed.window)
        }

        return combine(
            fetchedAt,
            fetchTracker.observe(feed),
        ) { at, state ->
            when {
                at != null -> TrendingFeedFetchStatus.Settled
                state == TrendingFetchTracker.State.Failed -> TrendingFeedFetchStatus.Failed
                else -> TrendingFeedFetchStatus.Loading
            }
        }
    }
}
