package nl.rhaydus.softcover.feature.reading.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapLatest
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.library.domain.usecase.GetUserBooksAsFlowUseCase

@OptIn(ExperimentalCoroutinesApi::class)
class GetCurrentlyReadingBooksUseCase(
    private val getUserBooksAsFlowUseCase: GetUserBooksAsFlowUseCase,
) {
    suspend operator fun invoke(): Flow<List<Book>> {
        return getUserBooksAsFlowUseCase().mapLatest { books: List<Book> ->
            books.filter { book: Book ->
                book.status == BookStatus.Reading
            }
        }
    }
}