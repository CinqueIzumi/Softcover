package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class ResetUserDataUSeCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            settingsRepository.updateApiKey(key = "")

            settingsRepository.updateUserId(id = -1)
        }
    }
}