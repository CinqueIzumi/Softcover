package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class ResetUserDataUseCase(
    private val settingsRepository: SettingsRepository,
    private val cachingRepository: CachingRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            settingsRepository.updateApiKey(key = "")

            cachingRepository.removeAllBooks()

            settingsRepository.updateUserId(id = -1)
        }
    }
}