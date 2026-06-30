package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.core.domain.result.runCatchingLogged
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetDynamicColorUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> = runCatchingLogged {
        settingsRepository.setDynamicColorEnabled(enabled = enabled)
    }
}
