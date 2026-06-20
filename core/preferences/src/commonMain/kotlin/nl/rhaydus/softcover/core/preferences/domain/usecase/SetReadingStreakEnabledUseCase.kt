package nl.rhaydus.softcover.core.preferences.domain.usecase

import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetReadingStreakEnabledUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> = runCatching {
        settingsRepository.setReadingStreakEnabled(enabled = enabled)
    }
}
