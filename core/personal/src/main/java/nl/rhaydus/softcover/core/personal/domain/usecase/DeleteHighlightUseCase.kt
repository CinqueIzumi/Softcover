package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.softcover.core.personal.domain.repository.HighlightRepository

class DeleteHighlightUseCase(
    private val repository: HighlightRepository,
) {
    suspend operator fun invoke(id: Long) = repository.delete(id = id)
}
