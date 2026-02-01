package nl.rhaydus.softcover.feature.caching.domain.usecase

import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase

class InitializeUserBooksUseCase(
    private val cachingRepository: CachingRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(): Result<Unit> = runCatching {
        val userId: Int = getUserIdUseCase().getOrThrow()

        cachingRepository.initializeBooks(userId = userId)
    }
}