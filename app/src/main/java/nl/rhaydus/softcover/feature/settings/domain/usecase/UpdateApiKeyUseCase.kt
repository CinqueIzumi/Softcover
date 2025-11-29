package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateApiKeyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(key: String): Result<Unit> {
        return runCatching {
            settingsRepository.updateApiKey(key = key)
        }
    }
}