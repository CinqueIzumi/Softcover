package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveRecommendedBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book

private const val RECOMMENDED_BOOKS_DISPLAY_LIMIT = 25

/**
 * The reader's Hardcover recommendations in rank order, minus anything they own ([observeOwnedBookIds]),
 * capped at [RECOMMENDED_BOOKS_DISPLAY_LIMIT]. The cache holds more than is shown so shelving a book
 * backfills the rail rather than shrinking it.
 */
class GetRecommendedBooksUseCase(
    private val observeRecommendedBooks: ObserveRecommendedBooksUseCase,
    private val observeOwnedBookIds: ObserveOwnedBookIdsUseCase,
) {
    operator fun invoke(): Flow<List<Book>> = combine(
        observeRecommendedBooks(),
        observeOwnedBookIds(),
    ) { recommended, ownedBookIds ->
        recommended
            .filterNot { book -> book.id in ownedBookIds }
            .take(RECOMMENDED_BOOKS_DISPLAY_LIMIT)
    }
}
