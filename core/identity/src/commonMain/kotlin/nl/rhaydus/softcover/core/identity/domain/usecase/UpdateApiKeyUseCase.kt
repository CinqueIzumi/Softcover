package nl.rhaydus.softcover.core.identity.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class UpdateApiKeyUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(key: String): Result<Unit> {
        return runCatchingLogged {
            settingsRepository.updateApiKey(key = key)
        }
    }
}
