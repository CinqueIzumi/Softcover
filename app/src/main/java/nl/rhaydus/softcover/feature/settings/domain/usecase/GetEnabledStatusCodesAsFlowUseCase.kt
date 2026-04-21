package nl.rhaydus.softcover.feature.settings.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class GetEnabledStatusCodesAsFlowUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<Set<Int>> = settingsRepository.enabledStatusCodes
}
