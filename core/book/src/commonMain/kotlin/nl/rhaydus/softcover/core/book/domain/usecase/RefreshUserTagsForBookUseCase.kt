package nl.rhaydus.softcover.core.book.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.book.domain.repository.UserTagsRepository
import nl.rhaydus.softcover.core.identity.domain.usecase.GetUserIdUseCase

class RefreshUserTagsForBookUseCase(
    private val userTagsRepository: UserTagsRepository,
    private val getUserIdUseCase: GetUserIdUseCase,
) {
    suspend operator fun invoke(bookId: Int): Result<Unit> = runCatchingLogged {
        val userId = getUserIdUseCase().getOrThrow()

        userTagsRepository.refreshForBook(
            userId = userId,
            bookId = bookId,
        )
    }
}
