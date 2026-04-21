package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class SetEnabledListIdsUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(ids: Set<Int>): Result<Unit> = runCatching {
        settingsRepository.setEnabledListIds(ids = ids)
    }
}
