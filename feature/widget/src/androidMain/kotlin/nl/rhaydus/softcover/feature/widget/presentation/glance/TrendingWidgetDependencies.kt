package nl.rhaydus.softcover.feature.widget.presentation.glance

import nl.rhaydus.softcover.core.book.domain.usecase.ObserveTrendingBooksUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.RefreshTrendingBooksUseCase
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdAsFlowUseCase
import nl.rhaydus.softcover.core.preferences.domain.usecase.GetThemeConfigurationUseCase
import nl.rhaydus.softcover.core.presentation.navigation.AppEntryPoint
import nl.rhaydus.softcover.feature.widget.data.datastore.TrendingStore

internal class TrendingWidgetDependencies(
    val getUserIdAsFlow: GetUserIdAsFlowUseCase,
    val getThemeConfiguration: GetThemeConfigurationUseCase,
    val observeTrendingBooks: ObserveTrendingBooksUseCase,
    val refreshTrendingBooks: RefreshTrendingBooksUseCase,
    val store: TrendingStore,
    val appEntryPoint: AppEntryPoint,
)
