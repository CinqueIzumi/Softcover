package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class GetLibrarySortSettingsAsFlowUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<Map<String, LibrarySortSettings>> =
        settingsRepository.librarySortSettingsByTab
}
