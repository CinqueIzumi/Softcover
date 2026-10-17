package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingPosition
import nl.rhaydus.softcover.feature.widget.domain.repository.TrendingRepository

internal class GetTrendingPositionUseCase(private val trendingRepository: TrendingRepository) {
    suspend operator fun invoke(appWidgetId: Int): Result<TrendingPosition> = runCatchingCancellable {
        trendingRepository.getPosition(appWidgetId = appWidgetId)
    }
}
