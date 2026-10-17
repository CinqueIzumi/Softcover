package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettings
import nl.rhaydus.softcover.feature.widget.domain.repository.TrendingRepository

internal class GetPlacedTrendingSettingsUseCase(private val trendingRepository: TrendingRepository) {
    suspend operator fun invoke(appWidgetIds: List<Int>): Result<List<TrendingSettings>> = runCatchingCancellable {
        appWidgetIds.map { appWidgetId -> trendingRepository.getSettings(appWidgetId = appWidgetId) }
    }
}
