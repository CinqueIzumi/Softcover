package nl.rhaydus.softcover.core.identity.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class GetUserIdAsFlowUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<Int> {
        return settingsRepository.getUserId()
    }
}
