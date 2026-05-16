package nl.rhaydus.softcover.feature.personal.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.personal.domain.model.Highlight
import nl.rhaydus.softcover.feature.personal.domain.repository.HighlightRepository

class ObserveAllHighlightsUseCase(
    private val repository: HighlightRepository,
) {
    operator fun invoke(): Flow<List<Highlight>> = repository.observeAll()
}
