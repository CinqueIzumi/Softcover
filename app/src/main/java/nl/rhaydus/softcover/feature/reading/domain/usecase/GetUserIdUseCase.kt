package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class GetUserIdUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(): Result<Int> {
        return runCatching { settingsRepository.getUserId() }
    }
}