package nl.rhaydus.softcover.core.preferences.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

/**
 * Null means the user has not overridden the "because you read" genre - the caller should fall
 * back to the auto-derived default.
 */
class GetBecauseYouReadGenreAsFlowUseCase(
    private val settingsRepository: SettingsRepository,
) {
    operator fun invoke(): Flow<String?> = settingsRepository.becauseYouReadGenre
}
