package nl.rhaydus.softcover.feature.settings.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class GetLibrarySortSettingsAsFlowUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<Map<String, LibrarySortSettings>> =
        settingsRepository.librarySortSettingsByTab
}
