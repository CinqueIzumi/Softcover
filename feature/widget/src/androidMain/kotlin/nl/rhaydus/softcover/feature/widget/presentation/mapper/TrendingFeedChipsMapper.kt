package nl.rhaydus.softcover.feature.widget.presentation.mapper

import nl.rhaydus.softcover.core.component.chip.ChipSet
import nl.rhaydus.softcover.core.domain.model.RecentWindow
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod
import nl.rhaydus.softcover.core.domain.model.UpcomingWindow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeed
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingFeedKind
import nl.rhaydus.softcover.feature.widget.domain.model.kind

internal fun trendingKindChipsFor(selected: TrendingFeedKind): ChipSet<TrendingFeedKind> = listOf(
    TrendingFeedKind.Trending,
    TrendingFeedKind.Recent,
    TrendingFeedKind.Upcoming,
).map { it.label() to it }.toChipSet(selected = selected)

internal fun trendingFeedChipsFor(selected: TrendingFeed): ChipSet<TrendingFeed> = TrendingFeed.All
    .filter { it.kind == selected.kind }
    .map { it.label() to it }
    .toChipSet(selected = selected)

internal fun TrendingFeedKind.label(): String = when (this) {
    TrendingFeedKind.Trending -> "Trending"
    TrendingFeedKind.Recent -> "Recent"
    TrendingFeedKind.Upcoming -> "Upcoming"
}

internal fun TrendingFeed.label(): String = when (this) {
    is TrendingFeed.Trending -> period.label()
    is TrendingFeed.Recent -> window.label()
    is TrendingFeed.Upcoming -> window.label()
}

private fun TrendingPeriod.label(): String = when (this) {
    TrendingPeriod.Week -> "This week"
    TrendingPeriod.Month -> "This month"
    TrendingPeriod.ThreeMonths -> "Past 3 months"
    TrendingPeriod.Year -> "Past year"
    TrendingPeriod.AllTime -> "All time"
}

private fun RecentWindow.label(): String = when (this) {
    RecentWindow.Week -> "Past week"
    RecentWindow.Month -> "Past month"
    RecentWindow.ThreeMonths -> "Past 3 months"
    RecentWindow.SixMonths -> "Past 6 months"
}

private fun UpcomingWindow.label(): String = when (this) {
    UpcomingWindow.Month -> "Next month"
    UpcomingWindow.ThreeMonths -> "Next 3 months"
    UpcomingWindow.SixMonths -> "Next 6 months"
    UpcomingWindow.Year -> "Next year"
}
