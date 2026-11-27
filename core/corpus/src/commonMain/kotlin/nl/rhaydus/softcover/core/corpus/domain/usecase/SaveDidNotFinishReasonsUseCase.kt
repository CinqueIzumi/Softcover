package nl.rhaydus.softcover.core.corpus.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.corpus.domain.repository.DidNotFinishReasonsRepository
import nl.rhaydus.softcover.core.domain.model.DidNotFinishReasons

class SaveDidNotFinishReasonsUseCase(
    private val repository: DidNotFinishReasonsRepository,
) {
    suspend operator fun invoke(reasons: DidNotFinishReasons): Result<Unit> = runCatchingLogged {
        repository.save(reasons = reasons)
    }
}
