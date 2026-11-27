package nl.rhaydus.softcover.core.corpus.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.corpus.domain.repository.MoodMomentsRepository
import nl.rhaydus.softcover.core.domain.model.MoodMoment

class ObserveMoodMomentsUseCase(
    private val repository: MoodMomentsRepository,
) {
    operator fun invoke(bookId: Int): Flow<List<MoodMoment>> = repository.observe(bookId = bookId)
}
