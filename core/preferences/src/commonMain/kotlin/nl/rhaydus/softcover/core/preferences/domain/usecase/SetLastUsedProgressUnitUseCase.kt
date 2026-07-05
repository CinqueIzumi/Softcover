package nl.rhaydus.softcover.core.preferences.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetLastUsedProgressUnitUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(unit: ProgressUnit): Result<Unit> = runCatchingLogged {
        settingsRepository.setLastUsedProgressUnit(unit = unit)
    }
}
