package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class SetLibrarySortUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(
        tabId: String,
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Result<Unit> = runCatching {
        settingsRepository.setLibrarySortForTab(
            tabId = tabId,
            mode = mode,
            direction = direction,
        )
    }
}
