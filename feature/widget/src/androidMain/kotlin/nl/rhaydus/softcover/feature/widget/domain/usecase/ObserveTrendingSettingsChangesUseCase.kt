package nl.rhaydus.softcover.feature.widget.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.widget.domain.model.TrendingSettingsChange
import nl.rhaydus.softcover.feature.widget.domain.repository.TrendingRepository

internal class ObserveTrendingSettingsChangesUseCase(private val trendingRepository: TrendingRepository) {
    operator fun invoke(): Flow<TrendingSettingsChange> = trendingRepository.settingsChanges
}
