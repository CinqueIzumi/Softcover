package nl.rhaydus.softcover.core.preferences.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.DesktopWindowState
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetDesktopWindowStateUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(state: DesktopWindowState): Result<Unit> = runCatchingLogged {
        settingsRepository.setDesktopWindowState(state = state)
    }
}
