package nl.rhaydus.softcover.feature.book_detail.domain.usecase

import nl.rhaydus.softcover.core.domain.model.UserTag
import nl.rhaydus.softcover.core.domain.result.runCatchingLogged
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase
import nl.rhaydus.softcover.feature.book_detail.domain.repository.UserTagsRepository

/**
 * Loads the current user's own tags on a book. The taggings query is keyed by the signed-in user,
 * so the user id is resolved here rather than threaded through the presentation layer.
 */
class GetUserTagsUseCase(
    private val userTagsRepository: UserTagsRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(bookId: Int): Result<List<UserTag>> = runCatchingLogged {
        val userId = getUserIdUseCase().getOrThrow()

        userTagsRepository.getUserTags(
            userId = userId,
            bookId = bookId,
        )
    }
}
