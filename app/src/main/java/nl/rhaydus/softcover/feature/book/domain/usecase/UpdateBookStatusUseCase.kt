package nl.rhaydus.softcover.feature.book.domain.usecase

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.enum.BookStatus
import nl.rhaydus.softcover.feature.book.domain.repository.BookDetailRepository
import nl.rhaydus.softcover.feature.settings.domain.usecase.GetUserIdUseCase

class UpdateBookStatusUseCase(
    private val bookDetailRepository: BookDetailRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(
        book: Book,
        newStatus: BookStatus,
    ) {
        val userId = getUserIdUseCase().getOrThrow()

        bookDetailRepository.updateBookStatus(
            book = book,
            newStatus = newStatus,
            userId = userId,
        )
    }
}