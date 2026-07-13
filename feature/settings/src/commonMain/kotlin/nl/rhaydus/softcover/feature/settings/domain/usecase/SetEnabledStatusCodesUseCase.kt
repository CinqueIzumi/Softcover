package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetEnabledStatusCodesUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(codes: Set<Int>): Result<Unit> = runCatchingLogged {
        settingsRepository.setEnabledStatusCodes(codes = codes)
    }
}
