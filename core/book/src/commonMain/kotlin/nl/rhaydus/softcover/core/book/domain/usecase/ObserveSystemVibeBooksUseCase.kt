package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.SystemVibe

class ObserveSystemVibeBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    operator fun invoke(vibe: SystemVibe): Flow<List<Book>> = booksRepository.observeSystemVibeBooks(vibe = vibe)
}
