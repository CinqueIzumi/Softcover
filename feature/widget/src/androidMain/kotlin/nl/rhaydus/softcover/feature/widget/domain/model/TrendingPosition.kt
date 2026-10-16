package nl.rhaydus.softcover.feature.widget.domain.model

import nl.rhaydus.softcover.core.domain.model.TrendingPeriod

/** Which period an instance shows and the page it is on; the page may be stale against a shorter list. */
internal data class TrendingPosition(
    val period: TrendingPeriod,
    val page: Int,
)
