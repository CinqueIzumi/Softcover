package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetDateStyleUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(newStyle: DateStyle): Result<Unit> = runCatchingLogged {
        settingsRepository.setDateStyle(style = newStyle)
    }
}
