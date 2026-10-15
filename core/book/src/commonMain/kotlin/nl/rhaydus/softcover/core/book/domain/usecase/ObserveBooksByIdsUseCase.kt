package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

class ObserveBooksByIdsUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(ids: List<Int>): Flow<List<Book>> = booksRepository.observeBooksByIds(ids = ids)
}
