package nl.rhaydus.softcover.core.preferences.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetHideBooksWithoutDetailUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(enabled: Boolean): Result<Unit> = runCatchingLogged {
        settingsRepository.setHideBooksWithoutDetail(enabled = enabled)
    }
}
