package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

class GetTrendingBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    suspend operator fun invoke(): Result<List<Book>> = runCatchingLogged {
        booksRepository.fetchTrendingBooks()
    }
}
