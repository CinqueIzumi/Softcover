package nl.rhaydus.softcover.core.preferences.domain.usecase

import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class SetLibraryGridLayoutUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(newLayout: LibraryGridLayout): Result<Unit> = runCatching {
        settingsRepository.setLibraryGridLayout(layout = newLayout)
    }
}
