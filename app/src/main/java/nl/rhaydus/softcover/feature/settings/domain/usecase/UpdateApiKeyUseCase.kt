package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class UpdateApiKeyUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(key: String): Result<Unit> {
        return runCatching {
            settingsRepository.updateApiKey(key = key)
        }
    }
}