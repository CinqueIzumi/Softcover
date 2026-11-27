package nl.rhaydus.softcover.core.corpus.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.corpus.domain.repository.BookMoodsRepository
import nl.rhaydus.softcover.core.domain.model.BookMood

class ObserveBookMoodsUseCase(
    private val repository: BookMoodsRepository,
) {
    operator fun invoke(bookId: Int): Flow<List<BookMood>> = repository.observe(bookId = bookId)
}
