package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class GetUserProfileDataUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): Result<UserProfileData> = runCatching {
        settingsRepository.getUserProfileData()
    }
}