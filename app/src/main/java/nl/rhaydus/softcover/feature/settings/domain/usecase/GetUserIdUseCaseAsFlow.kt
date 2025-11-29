package nl.rhaydus.softcover.feature.settings.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class GetUserIdUseCaseAsFlow @Inject constructor(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<Int> {
        return settingsRepository.getUserId()
    }
}