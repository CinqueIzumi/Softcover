package nl.rhaydus.softcover.feature.widget.domain.usecase

import nl.rhaydus.common.runCatchingCancellable
import nl.rhaydus.softcover.feature.widget.domain.repository.TrendingRepository

internal class AdvanceTrendingPageUseCase(private val trendingRepository: TrendingRepository) {
    suspend operator fun invoke(
        appWidgetId: Int,
        pageCount: Int,
        step: Int,
    ): Result<Unit> = runCatchingCancellable {
        trendingRepository.advancePage(
            appWidgetId = appWidgetId,
            pageCount = pageCount,
            step = step,
        )
    }
}
