package nl.rhaydus.softcover.feature.settings.domain.usecase

import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import nl.rhaydus.softcover.feature.books.domain.repository.BooksRepository

class InitializeUserIdAndBooksUseCase(
    private val settingsRepository: SettingsRepository,
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val userId: Int = settingsRepository.getUserIdFromBackend()

        booksRepository.refreshUserBooks(userId = userId, scope = RefreshScope.All)

        settingsRepository.updateUserId(id = userId)
    }
}