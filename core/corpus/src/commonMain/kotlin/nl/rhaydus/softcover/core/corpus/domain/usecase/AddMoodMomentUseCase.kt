package nl.rhaydus.softcover.core.corpus.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.corpus.domain.repository.MoodMomentsRepository
import nl.rhaydus.softcover.core.domain.model.MoodWord

class AddMoodMomentUseCase(
    private val repository: MoodMomentsRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        word: MoodWord,
        strength: Int?,
        percent: Int?,
        page: Int?,
        seconds: Int?,
    ): Result<Unit> = runCatchingLogged {
        repository.add(
            bookId = bookId,
            word = word,
            strength = strength,
            percent = percent,
            page = page,
            seconds = seconds,
        )
    }
}
