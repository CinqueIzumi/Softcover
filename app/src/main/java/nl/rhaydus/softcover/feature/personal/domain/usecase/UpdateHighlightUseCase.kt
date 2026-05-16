package nl.rhaydus.softcover.feature.personal.domain.usecase

import nl.rhaydus.softcover.feature.personal.domain.model.Highlight
import nl.rhaydus.softcover.feature.personal.domain.repository.HighlightRepository

class UpdateHighlightUseCase(
    private val repository: HighlightRepository,
) {
    suspend operator fun invoke(highlight: Highlight) = repository.update(highlight = highlight)
}
