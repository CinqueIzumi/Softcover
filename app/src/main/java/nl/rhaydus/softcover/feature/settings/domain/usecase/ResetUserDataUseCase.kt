package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class ResetUserDataUseCase(
    private val settingsRepository: SettingsRepository,
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        return runCatching {
            settingsRepository.updateApiKey(key = "")

            booksRepository.removeAllBooks()

            settingsRepository.resetLibraryVisibilityPreferences()

            settingsRepository.updateUserId(id = -1)
        }
    }
}