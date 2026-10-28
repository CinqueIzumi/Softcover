package nl.rhaydus.softcover.core.book.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.book.domain.repository.UserTagsRepository
import nl.rhaydus.softcover.core.domain.model.UserTag

class ObserveUserTagsByBookUseCase(
    private val userTagsRepository: UserTagsRepository,
) {
    operator fun invoke(): Flow<Map<Int, List<UserTag>>> = userTagsRepository.observeAll()
}
