package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.domain.result.runCatchingLogged
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagsRepository

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
        userTagsRepository.saveTags(
            bookId = bookId,
            tags = tags,
        )
    }
}
