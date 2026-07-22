package nl.rhaydus.softcover.core.preferences.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository

/** Pass null to clear the override and fall back to the auto-derived genre. */
class SetBecauseYouReadGenreUseCase(
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(genre: String?): Result<Unit> = runCatchingLogged {
        settingsRepository.setBecauseYouReadGenre(genre = genre)
    }
}
