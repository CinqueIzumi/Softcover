package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettings
import nl.rhaydus.softcover.feature.widget.domain.repository.TrendingRepository

internal class GetTrendingSettingsUseCase(private val trendingRepository: TrendingRepository) {
    suspend operator fun invoke(appWidgetId: Int): Result<TrendingSettings> = runCatchingCancellable {
        trendingRepository.getSettings(appWidgetId = appWidgetId)
    }
}
