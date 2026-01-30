package nl.rhaydus.softcover.feature.book.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase


// TODO: I want to try and make the app offline-first, with all data being loaded initially on start-up (if data exists)
//  add support for refresh (with api call), apart from that only search / updating book progress should be done using internet
//  when updating a book, use the return of the given data to make sure that the local values are updated.

class FetchBookByIdUseCase(
    private val bookDetailRepository: BookDetailRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(id: Int): Result<Book> {
        return runCatching {
            val userId = getUserIdUseCase().getOrThrow()

            bookDetailRepository.fetchBookById(
                id = id,
                userId = userId,
            )
        }
    }
}