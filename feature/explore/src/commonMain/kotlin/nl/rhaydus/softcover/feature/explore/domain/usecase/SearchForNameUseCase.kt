package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.explore.domain.model.ExploreSortMode
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class SearchForNameUseCase(
    private val searchRepository: ExploreRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    /**
     * [page] is 1-based; page 1 replaces the results and records [name] in search history, a
     * later page appends without re-recording the same query.
     */
    suspend operator fun invoke(
        name: String,
        sortMode: ExploreSortMode,
        page: Int = 1,
    ): Result<Unit> = runCatchingLogged {
        val userId = getUserIdUseCase().getOrThrow()

        searchRepository.searchForName(
            name = name,
            userId = userId,
            sortMode = sortMode,
            page = page,
        )

        if (page <= 1) {
            searchRepository.saveSearchQuery(name = name)
        }
    }
}
