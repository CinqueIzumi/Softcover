package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class GetQueriedBooksHasMoreUseCase(
    private val searchRepository: ExploreRepository,
) {
    operator fun invoke(): Flow<Boolean> = searchRepository.queriedBooksHasMore
}
