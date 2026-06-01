package nl.rhaydus.softcover.orchestration.usecase

import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.account.ResetUserDataUseCase
import nl.rhaydus.softcover.core.preferences.domain.repository.SettingsRepository
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

class ResetUserDataUseCaseImpl(
    private val settingsRepository: SettingsRepository,
    private val booksRepository: BooksRepository,
    private val profileRepository: ProfileRepository,
) : ResetUserDataUseCase {
    override suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            settingsRepository.updateApiKey(key = "")

            booksRepository.removeAllBooks()

            profileRepository.clearProfileCache()

            settingsRepository.resetLibraryVisibilityPreferences()

            settingsRepository.updateUserId(id = -1)
        }
    }
}
