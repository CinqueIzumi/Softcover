package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.core.domain.result.runCatchingLogged
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetEnabledListIdsUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(ids: Set<Int>): Result<Unit> = runCatchingLogged {
        settingsRepository.setEnabledListIds(ids = ids)
    }
}
