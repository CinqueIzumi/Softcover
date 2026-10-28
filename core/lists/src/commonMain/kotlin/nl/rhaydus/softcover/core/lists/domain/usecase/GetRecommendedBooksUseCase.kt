package nl.rhaydus.softcover.core.lists.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveSystemVibeBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.SystemVibe

/**
 * The books on a [SystemVibe] in rank order, minus anything the reader owns ([observeOwnedBookIds]) or has
 * hidden ([observeHiddenRecommendations]).
 * The cache holds more than a caller shows, so shelving or hiding a book backfills the list rather than
 * shrinking it.
 */
class GetRecommendedBooksUseCase internal constructor(
    private val observeSystemVibeBooks: ObserveSystemVibeBooksUseCase,
    private val observeOwnedBookIds: ObserveOwnedBookIdsUseCase,
    private val observeHiddenRecommendations: ObserveHiddenRecommendationsUseCase,
) {
    operator fun invoke(
        vibe: SystemVibe,
        limit: Int,
    ): Flow<List<Book>> = combine(
        observeSystemVibeBooks(vibe = vibe),
        observeOwnedBookIds(),
        observeHiddenRecommendations(),
    ) { vibeBooks, ownedBookIds, hiddenRecommendations ->
        val excludedBookIds = ownedBookIds + hiddenRecommendations.map { it.bookId }

        vibeBooks
            .filterNot { book -> book.id in excludedBookIds }
            .take(limit)
    }
}
