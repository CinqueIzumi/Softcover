package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

/*
 * Created by Bart Bos on 29/11/2025 at 20:37
 */

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