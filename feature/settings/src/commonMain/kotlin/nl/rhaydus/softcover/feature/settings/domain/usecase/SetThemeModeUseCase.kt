package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetThemeModeUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(mode: ThemeMode): Result<Unit> = runCatchingLogged {
        settingsRepository.setThemeMode(mode = mode)
    }
}
