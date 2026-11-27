package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetUsualCurrencyUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(code: String): Result<Unit> = runCatchingLogged {
        settingsRepository.setUsualCurrencyCode(code = code)
    }
}
