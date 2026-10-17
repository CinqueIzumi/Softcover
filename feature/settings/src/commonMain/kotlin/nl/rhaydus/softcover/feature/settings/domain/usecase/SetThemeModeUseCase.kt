package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

/**
 * Writes the chosen theme mode — and, for [ThemeMode.DIM], switches **dynamic colour off** in the
 * same call, because Dim paints its own colours: leaving dynamic colour on would claim a wallpaper
 * look the scheme never shows. The other modes leave dynamic colour as the reader set it.
 */
class SetThemeModeUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(mode: ThemeMode): Result<Unit> = runCatchingLogged {
        settingsRepository.setThemeMode(mode = mode)
        if (mode == ThemeMode.DIM) {
            settingsRepository.setDynamicColorEnabled(enabled = false)
        }
    }
}
