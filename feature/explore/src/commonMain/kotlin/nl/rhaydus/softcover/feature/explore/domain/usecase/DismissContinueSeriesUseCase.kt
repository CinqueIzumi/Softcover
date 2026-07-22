package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class DismissContinueSeriesUseCase(
    private val exploreRepository: ExploreRepository,
) {
    suspend operator fun invoke(
        seriesId: Int,
        seriesName: String? = null,
        coverUrl: String? = null,
        authorText: String? = null,
        bookCount: Int? = null,
    ): Result<Unit> = runCatchingLogged {
        exploreRepository.dismissContinueSeries(
            seriesId = seriesId,
            seriesName = seriesName,
            coverUrl = coverUrl,
            authorText = authorText,
            bookCount = bookCount,
        )
    }
}
