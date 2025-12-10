package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import javax.inject.Inject

class UpdateBookProgressUseCase @Inject constructor(
    private val repository: BooksRepository,
) {
    suspend operator fun invoke(
        book: BookWithProgress,
        newPage: Int,
    ): Result<Unit> = runCatching {
        repository.updateBookProgress(
            book = book,
            newPage = newPage,
        )
    }
}