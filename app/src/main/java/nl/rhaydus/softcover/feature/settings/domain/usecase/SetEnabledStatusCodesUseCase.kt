package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class SetEnabledStatusCodesUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(codes: Set<Int>): Result<Unit> = runCatching {
        settingsRepository.setEnabledStatusCodes(codes = codes)
    }
}
