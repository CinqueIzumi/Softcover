package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

/**
 * Writes the chosen spine colour — and switches **dynamic colour off** in the same call, because
 * while dynamic colour is on it replaces the whole palette with the wallpaper's scheme: tapping a
 * spine colour is an unambiguous request to see that look, so leaving the two on together would make
 * the tile a dead control. The palette itself is remembered either way, so a reader who turns dynamic
 * colour back off gets their palette back.
 */
class SetColorPaletteUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(palette: ColorPalette): Result<Unit> = runCatchingLogged {
        settingsRepository.setColorPalette(palette = palette)
        settingsRepository.setDynamicColorEnabled(enabled = false)
    }
}
