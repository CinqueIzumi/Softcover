package nl.rhaydus.softcover.feature.reading.domain.usecase

import nl.rhaydus.softcover.core.domain.exception.NoUserIdFoundException
import nl.rhaydus.softcover.feature.reading.domain.model.BookWithProgress
import nl.rhaydus.softcover.feature.reading.domain.repository.BooksRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase
import javax.inject.Inject

class GetCurrentlyReadingBooksUseCase @Inject constructor(
    private val booksRepository: BooksRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(): Result<List<BookWithProgress>> {
        return runCatching {
            val userId = getUserIdUseCase().getOrDefault(-1)

            if (userId == -1) throw NoUserIdFoundException()

            booksRepository.getCurrentlyReadingBooks(userId = userId)
        }
    }
}