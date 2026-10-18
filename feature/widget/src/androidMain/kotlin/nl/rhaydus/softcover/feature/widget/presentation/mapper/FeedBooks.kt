package nl.rhaydus.softcover.feature.widget.presentation.mapper

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeedFetchStatus
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition

/** [fetchStatus] is only ever other than [TrendingFeedFetchStatus.Settled] for an empty [books]. */
internal data class FeedBooks(
    val position: TrendingPosition,
    val books: List<Book>,
    val fetchStatus: TrendingFeedFetchStatus,
)
