package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.TrendingPeriod

class ObserveTrendingBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(period: TrendingPeriod): Flow<List<Book>> =
        booksRepository.observeTrendingBooks(period = period)
}
