package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetBottomBarStyleUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(newStyle: BottomBarStyle): Result<Unit> = runCatching {
        settingsRepository.setBottomBarStyle(style = newStyle)
    }
}