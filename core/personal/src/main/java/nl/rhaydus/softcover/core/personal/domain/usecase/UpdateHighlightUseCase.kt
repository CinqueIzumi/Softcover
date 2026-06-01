package nl.rhaydus.softcover.core.personal.domain.usecase

import nl.rhaydus.softcover.core.personal.domain.model.Highlight
import nl.rhaydus.softcover.core.personal.domain.repository.HighlightRepository

class UpdateHighlightUseCase(
    private val repository: HighlightRepository,
) {
    suspend operator fun invoke(highlight: Highlight) = repository.update(highlight = highlight)
}
