package nl.rhaydus.softcover.core.corpus.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.corpus.domain.repository.DidNotFinishReasonsRepository
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons

class ObserveAllDidNotFinishReasonsUseCase(
    private val repository: DidNotFinishReasonsRepository,
) {
    operator fun invoke(): Flow<Map<Int, DidNotFinishReasons>> = repository.observeAll()
}
