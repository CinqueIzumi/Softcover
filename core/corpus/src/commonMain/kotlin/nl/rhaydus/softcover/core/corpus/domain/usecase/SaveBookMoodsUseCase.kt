package nl.rhaydus.softcover.core.corpus.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.corpus.domain.repository.BookMoodsRepository
import nl.rhaydus.softcover.core.domain.model.BookMood

class SaveBookMoodsUseCase(
    private val repository: BookMoodsRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        moods: List<BookMood>,
    ): Result<Unit> = runCatchingLogged {
        repository.save(
            bookId = bookId,
            moods = moods,
        )
    }
}
