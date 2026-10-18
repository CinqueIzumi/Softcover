package nl.rhaydus.softcover.feature.widget.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveUpcomingBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed

internal class ObserveTrendingFeedUseCase(
    private val observeTrendingBooks: ObserveTrendingBooksUseCase,
    private val observeUpcomingBooks: ObserveUpcomingBooksUseCase,
) {
    operator fun invoke(feed: TrendingFeed): Flow<List<Book>> = when (feed) {
        is TrendingFeed.Trending -> observeTrendingBooks(period = feed.period)
        is TrendingFeed.Upcoming -> observeUpcomingBooks(window = feed.window)
    }
}
