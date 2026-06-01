package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

class GetLibraryTabOrderAsFlowUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<List<String>> = settingsRepository.libraryTabOrder
}
