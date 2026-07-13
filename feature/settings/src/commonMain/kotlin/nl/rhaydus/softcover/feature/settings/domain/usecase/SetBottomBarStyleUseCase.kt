package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetBottomBarStyleUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(newStyle: BottomBarStyle): Result<Unit> = runCatchingLogged {
        settingsRepository.setBottomBarStyle(style = newStyle)
    }
}
