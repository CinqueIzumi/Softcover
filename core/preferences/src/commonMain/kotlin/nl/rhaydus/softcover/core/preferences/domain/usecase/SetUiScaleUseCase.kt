package nl.rhaydus.softcover.core.preferences.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetUiScaleUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(scale: UiScale): Result<Unit> = runCatchingLogged {
        settingsRepository.setUiScale(scale = scale)
    }
}
