package nl.rhaydus.softcover.core.corpus.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.corpus.domain.repository.MoodMomentsRepository

class DeleteMoodMomentUseCase(
    private val repository: MoodMomentsRepository,
) {
    suspend operator fun invoke(id: Long): Result<Unit> = runCatchingLogged {
        repository.delete(id = id)
    }
}
