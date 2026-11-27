package nl.rhaydus.softcover.core.corpus.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.corpus.domain.repository.MoodMomentsRepository
import nl.rhaydus.softcover.core.domain.model.MoodMoment

class UpdateMoodMomentUseCase(
    private val repository: MoodMomentsRepository,
) {
    suspend operator fun invoke(moment: MoodMoment): Result<Unit> = runCatchingLogged {
        repository.update(moment = moment)
    }
}
