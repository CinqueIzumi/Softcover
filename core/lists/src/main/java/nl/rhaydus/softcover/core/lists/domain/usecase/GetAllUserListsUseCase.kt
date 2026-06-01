package nl.rhaydus.softcover.core.lists.domain.usecase

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.BookList
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository

class GetAllUserListsUseCase(
    private val listsRepository: ListsRepository,
) {
    operator fun invoke(): Flow<List<BookList>> = listsRepository.allUserLists
}
