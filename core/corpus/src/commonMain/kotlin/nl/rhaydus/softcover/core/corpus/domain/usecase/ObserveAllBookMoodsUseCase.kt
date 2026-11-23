package nl.rhaydus.softcover.core.corpus.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.corpus.domain.repository.BookMoodsRepository
import nl.rhaydus.softcover.core.domain.model.BookMood

class ObserveAllBookMoodsUseCase(
    private val repository: BookMoodsRepository,
) {
    operator fun invoke(): Flow<Map<Int, List<BookMood>>> = repository.observeAll()
}
