package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class SetLibrarySortModeUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(
        tabId: String,
        mode: LibrarySortMode,
    ): Result<Unit> = runCatching {
        settingsRepository.setLibrarySortModeForTab(
            tabId = tabId,
            mode = mode,
        )
    }
}
