package nl.rhaydus.softcover.core.identity.domain.usecase

import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class UpdateApiKeyUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(key: String): Result<Unit> {
        return runCatching {
            settingsRepository.updateApiKey(key = key)
        }
    }
}