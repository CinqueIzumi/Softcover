package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import javax.inject.Inject

class UpdateBookProgressUseCase @Inject constructor(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(
        book: Book,
        newPage: Int,
    ): Result<Unit> = runCatching {
        repository.updateBookProgress(
            book = book,
            newPage = newPage,
        )
    }
}