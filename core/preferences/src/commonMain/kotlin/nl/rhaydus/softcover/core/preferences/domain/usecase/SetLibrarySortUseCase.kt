package nl.rhaydus.softcover.core.preferences.domain.usecase

import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.result.runCatchingLogged
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetLibrarySortUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(
        tabId: String,
        mode: LibrarySortMode,
        direction: SortDirection,
    ): Result<Unit> = runCatchingLogged {
        settingsRepository.setLibrarySortForTab(
            tabId = tabId,
            mode = mode,
            direction = direction,
        )
    }
}
