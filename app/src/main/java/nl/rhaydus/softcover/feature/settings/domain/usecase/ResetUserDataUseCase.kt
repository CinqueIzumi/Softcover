package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.profile.domain.repository.ProfileRepository

class ResetUserDataUseCase(
    private val settingsRepository: SettingsRepository,
    private val booksRepository: BooksRepository,
    private val profileRepository: ProfileRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            settingsRepository.updateApiKey(key = "")

            booksRepository.removeAllBooks()

            profileRepository.clearProfileCache()

            settingsRepository.resetLibraryVisibilityPreferences()

            settingsRepository.updateUserId(id = -1)
        }
    }
}
