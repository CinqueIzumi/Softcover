package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class GetLibraryGridLayoutAsFlowUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<LibraryGridLayout> = settingsRepository.libraryGridLayout
}
