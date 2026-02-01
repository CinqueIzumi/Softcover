package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class InitializeUserIdUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            val userId = settingsRepository.getUserIdFromBackend()

            settingsRepository.updateUserId(id = userId)
        }
    }
}