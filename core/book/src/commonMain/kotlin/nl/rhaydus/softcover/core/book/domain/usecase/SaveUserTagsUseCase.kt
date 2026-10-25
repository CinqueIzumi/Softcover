package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.UserTagsRepository
import nl.rhaydus.softcover.core.domain.model.UserTag

/**
 * Persists the user's complete tag set for a book. The backend upsert replaces the whole set, so
 * every add, removal, or spoiler toggle re-sends the full list; the canonical set is returned.
 */
class SaveUserTagsUseCase(
    private val userTagsRepository: UserTagsRepository,
) {
    suspend operator fun invoke(
        bookId: Int,
        tags: List<UserTag>,
    ): Result<List<UserTag>> = runCatchingLogged {
        userTagsRepository.save(
            bookId = bookId,
            tags = tags,
        )
    }
}
