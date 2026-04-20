package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class SetLibraryGridLayoutUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(newLayout: LibraryGridLayout): Result<Unit> = runCatching {
        settingsRepository.setLibraryGridLayout(layout = newLayout)
    }
}
