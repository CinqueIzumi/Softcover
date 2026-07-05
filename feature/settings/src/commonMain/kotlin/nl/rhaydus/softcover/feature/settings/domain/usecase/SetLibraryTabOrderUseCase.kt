package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetLibraryTabOrderUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(order: List<String>): Result<Unit> = runCatchingLogged {
        settingsRepository.setLibraryTabOrder(order = order)
    }
}
