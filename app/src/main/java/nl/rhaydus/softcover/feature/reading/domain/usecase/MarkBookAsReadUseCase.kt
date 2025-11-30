package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import javax.inject.Inject

class MarkBookAsReadUseCase @Inject constructor(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(book: BookWithProgress): Result<Unit> = runCatching {
        repository.markBookAsRead(book = book)
    }
}