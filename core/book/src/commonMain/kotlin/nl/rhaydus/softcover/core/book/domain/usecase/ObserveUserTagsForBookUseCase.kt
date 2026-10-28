package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.UserTagsRepository
import nl.rhaydus.softcover.core.domain.model.UserTag

class ObserveUserTagsForBookUseCase(
    private val userTagsRepository: UserTagsRepository,
) {
    operator fun invoke(bookId: Int): Flow<List<UserTag>> = userTagsRepository.observeForBook(bookId = bookId)
}
