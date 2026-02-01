package nl.rhaydus.softcover.feature.caching.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.caching.domain.repository.CachingRepository

class GetDidNotFinishUserBooksUseCase(
    private val cachingRepository: CachingRepository,
) {
    operator fun invoke(): Flow<List<Book>> {
        return cachingRepository.getBooksFlowByStatus(status = UserBookStatus.DID_NOT_FINISH)
    }
}