package nl.rhaydus.softcover.feature.caching.domain.usecase

import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase

class RefreshUserBooksUseCase(
    private val getUserIdUseCase: GetUserIdUseCase,
    private val cachingRepository: CachingRepository,
) {
    suspend operator fun invoke() = runCatching {
        val userId = getUserIdUseCase().getOrThrow()

        cachingRepository.refreshUserBooks(userId = userId)
    }
}