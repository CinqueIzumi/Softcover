package nl.rhaydus.softcover.core.corpus.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.corpus.domain.repository.BookTraitsRepository
import nl.rhaydus.softcover.core.domain.model.BookTraits

class SaveBookTraitsUseCase(
    private val repository: BookTraitsRepository,
) {
    suspend operator fun invoke(traits: BookTraits): Result<Unit> = runCatchingLogged {
        repository.save(traits = traits)
    }
}
