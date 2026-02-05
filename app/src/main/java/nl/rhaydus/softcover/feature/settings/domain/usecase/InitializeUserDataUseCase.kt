package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository
import nl.rhaydus.softcover.feature.settings.domain.model.UserInformation
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class InitializeUserDataUseCase(
    private val settingsRepository: SettingsRepository,
    private val cachingRepository: CachingRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val information: UserInformation = settingsRepository.getUserInfoFromBackend()

        cachingRepository.initializeBooks(userId = information.id)

        settingsRepository.updateUserId(id = information.id)
    }
}