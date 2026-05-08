package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetTrendingBooksUseCase(
    private val exploreRepository: ExploreRepository,
) {
    suspend operator fun invoke(): Result<List<Book>> = runCatching {
        exploreRepository.fetchTrendingBooks()
    }
}
