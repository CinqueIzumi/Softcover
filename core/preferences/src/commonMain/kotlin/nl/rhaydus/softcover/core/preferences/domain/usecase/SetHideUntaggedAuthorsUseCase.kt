package nl.rhaydus.softcover.core.preferences.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetHideUntaggedAuthorsUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> = runCatchingLogged {
        settingsRepository.setHideUntaggedAuthors(enabled = enabled)
    }
}
