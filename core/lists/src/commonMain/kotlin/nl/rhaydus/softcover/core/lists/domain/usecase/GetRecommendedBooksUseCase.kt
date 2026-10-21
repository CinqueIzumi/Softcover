package nl.rhaydus.softcover.core.lists.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveRecommendedBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book

/**
 * The reader's Hardcover recommendations in rank order, minus anything they own ([observeOwnedBookIds]).
 * The cache holds more than a caller shows, so shelving a book backfills the list rather than shrinking it.
 */
class GetRecommendedBooksUseCase(
    private val observeRecommendedBooks: ObserveRecommendedBooksUseCase,
    private val observeOwnedBookIds: ObserveOwnedBookIdsUseCase,
) {
    operator fun invoke(limit: Int): Flow<List<Book>> = combine(
        observeRecommendedBooks(),
        observeOwnedBookIds(),
    ) { recommended, ownedBookIds ->
        recommended
            .filterNot { book -> book.id in ownedBookIds }
            .take(limit)
    }
}
