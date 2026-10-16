package nl.rhaydus.softcover.feature.widget.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.widget.domain.repository.TrendingRepository

internal class ObserveTrendingChangesUseCase(private val trendingRepository: TrendingRepository) {
    operator fun invoke(): Flow<Int> = trendingRepository.changes
}
