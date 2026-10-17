package nl.rhaydus.softcover.feature.widget.presentation.glance

import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshTrendingBooksUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.AdvanceTrendingPageUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.GetTrendingPositionUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveTrendingChangesUseCase
import nl.rhaydus.softcover.feature.widget.domain.usecase.ObserveTrendingSettingsChangesUseCase

internal class TrendingWidgetDependencies(
    val environment: WidgetEnvironment,
    val repainter: WidgetRepainter,
    val observeTrendingBooks: ObserveTrendingBooksUseCase,
    val refreshTrendingBooks: RefreshTrendingBooksUseCase,
    val getTrendingPosition: GetTrendingPositionUseCase,
    val advanceTrendingPage: AdvanceTrendingPageUseCase,
    val observeTrendingChanges: ObserveTrendingChangesUseCase,
    val observeTrendingSettingsChanges: ObserveTrendingSettingsChangesUseCase,
)
