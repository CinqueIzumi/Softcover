package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class GetApiKeyUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): Result<String> = runCatching {
        settingsRepository.getApiKey()
    }
}