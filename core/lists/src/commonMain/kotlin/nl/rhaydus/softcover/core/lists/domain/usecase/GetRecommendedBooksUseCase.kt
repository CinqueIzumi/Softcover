package nl.rhaydus.softcover.core.lists.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.book.domain.usecase.ObserveSystemVibeBooksUseCase
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.SystemVibe

/**
 * The books on a [SystemVibe] in rank order, minus anything the reader owns ([observeOwnedBookIds]).
 * The cache holds more than a caller shows, so shelving a book backfills the list rather than shrinking it.
 */
class GetRecommendedBooksUseCase(
    private val observeSystemVibeBooks: ObserveSystemVibeBooksUseCase,
    private val observeOwnedBookIds: ObserveOwnedBookIdsUseCase,
) {
    operator fun invoke(
        vibe: SystemVibe,
        limit: Int,
    ): Flow<List<Book>> = combine(
        observeSystemVibeBooks(vibe = vibe),
        observeOwnedBookIds(),
    ) { vibeBooks, ownedBookIds ->
        vibeBooks
            .filterNot { book -> book.id in ownedBookIds }
            .take(limit)
    }
}
