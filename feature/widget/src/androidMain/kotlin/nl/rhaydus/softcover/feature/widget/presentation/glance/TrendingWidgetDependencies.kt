package nl.rhaydus.softcover.feature.widget.presentation.glance

import nl.rhaydus.softcover.feature.widget.domain.usecase.AdvanceTrendingPageUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetTrendingPositionUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveTrendingFeedUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.RefreshTrendingFeedUseCase
import nl.rhaydus.softcover.feature.widget.presentation.refresh.TrendingFeedFetcher

internal class TrendingWidgetDependencies(
    val environment: WidgetEnvironment,
    val repainter: WidgetRepainter,
    val observeTrendingFeed: ObserveTrendingFeedUseCase,
    val refreshTrendingFeed: RefreshTrendingFeedUseCase,
    val fetcher: TrendingFeedFetcher,
    val getTrendingPosition: GetTrendingPositionUseCase,
    val advanceTrendingPage: AdvanceTrendingPageUseCase,
    val renderer: BackgroundWidgetRenderer,
)
