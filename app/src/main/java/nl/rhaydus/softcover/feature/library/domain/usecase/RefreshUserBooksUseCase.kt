package nl.rhaydus.softcover.feature.library.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import nl.rhaydus.softcover.feature.library.domain.repository.LibraryRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase

class RefreshUserBooksUseCase(
    private val libraryRepository: LibraryRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val userId = getUserIdUseCase().getOrThrow()

        libraryRepository.refreshUserBooks(userId = userId)
    }
}