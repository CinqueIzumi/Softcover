package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class GetLastUsedProgressUnitAsFlowUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<ProgressUnit> = settingsRepository.lastUsedProgressUnit
}
