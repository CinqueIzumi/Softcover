package nl.rhaydus.softcover.feature.explore.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.feature.explore.domain.repository.ExploreRepository

class DismissContinueSeriesBookUseCase(
    private val exploreRepository: ExploreRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        title: String? = null,
        coverUrl: String? = null,
        authorText: String? = null,
        seriesName: String? = null,
    ): Result<Unit> = runCatchingLogged {
        exploreRepository.dismissContinueSeriesBook(
            bookId = bookId,
            title = title,
            coverUrl = coverUrl,
            authorText = authorText,
            seriesName = seriesName,
        )
    }
}
