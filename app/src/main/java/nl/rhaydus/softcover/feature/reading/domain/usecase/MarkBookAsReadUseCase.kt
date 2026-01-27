package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import javax.inject.Inject

class MarkBookAsReadUseCase @Inject constructor(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(book: Book): Result<Unit> = runCatching {
        repository.markBookAsRead(book = book)
    }
}