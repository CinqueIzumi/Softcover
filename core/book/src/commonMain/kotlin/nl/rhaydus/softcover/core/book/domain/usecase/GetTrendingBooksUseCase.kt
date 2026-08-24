package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.domain.model.Book

// Backs BOTH the Reading tab's TrendingBooksLoader and the Explore tab's TrendingBooksCollector.
// The session cache that keeps trending to 2 requests per session lives in the repository, which is
// what decides whether a read is answered locally or from the network - see
// BooksRepository.fetchTrendingBooks.
class GetTrendingBooksUseCase(
    private val booksRepository: BooksRepository,
) {
    /**
     * [forceRefresh] bypasses the repository's session cache and refetches. Pass it only for an
     * explicit user-initiated refresh (pull-to-refresh): answering one from the cache spins the
     * indicator, changes nothing, and reports no error, which reads as a broken button. A collector
     * running on screen mount is not user intent and must leave it `false`.
     */
    suspend operator fun invoke(forceRefresh: Boolean = false): Result<List<Book>> = runCatchingLogged {
        booksRepository.fetchTrendingBooks(forceRefresh = forceRefresh)
    }
}
