package nl.rhaydus.softcover.core.book.data.mapper

import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.type.TrendingDuration

internal fun TrendingPeriod.toTrendingDuration(): TrendingDuration = when (this) {
    TrendingPeriod.Week -> TrendingDuration.week
    TrendingPeriod.Month -> TrendingDuration.month
    TrendingPeriod.ThreeMonths -> TrendingDuration.three_month
    TrendingPeriod.Year -> TrendingDuration.one_year
    TrendingPeriod.AllTime -> TrendingDuration.all
}

internal fun TrendingPeriod.toStorageKey(): String = toTrendingDuration().rawValue
