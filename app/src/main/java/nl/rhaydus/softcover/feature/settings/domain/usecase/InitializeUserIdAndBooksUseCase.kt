package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class InitializeUserIdAndBooksUseCase(
    private val settingsRepository: SettingsRepository,
    private val cachingRepository: CachingRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val userId: Int = settingsRepository.getUserIdFromBackend()

        cachingRepository.initializeBooks(userId = userId)

        settingsRepository.updateUserId(id = userId)
    }
}