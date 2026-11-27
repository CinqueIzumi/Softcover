package nl.rhaydus.softcover.core.corpus.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.corpus.domain.repository.DidNotFinishReasonsRepository
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons

class GetDidNotFinishReasonsUseCase(
    private val repository: DidNotFinishReasonsRepository,
) {
    suspend operator fun invoke(bookId: Int): Result<DidNotFinishReasons> = runCatchingLogged {
        repository.get(bookId = bookId)
    }
}
