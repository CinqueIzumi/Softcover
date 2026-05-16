package nl.rhaydus.softcover.feature.personal.domain.usecase

import nl.rhaydus.softcover.feature.personal.domain.repository.HighlightRepository

class DeleteHighlightUseCase(
    private val repository: HighlightRepository,
) {
    suspend operator fun invoke(id: Long) = repository.delete(id = id)
}
