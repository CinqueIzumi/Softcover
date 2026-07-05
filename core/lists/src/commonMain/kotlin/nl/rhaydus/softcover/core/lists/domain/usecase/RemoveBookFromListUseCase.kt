package nl.rhaydus.softcover.core.lists.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository

class RemoveBookFromListUseCase(
    private val listsRepository: ListsRepository,
) {
    suspend operator fun invoke(
        listId: Int,
        bookId: Int,
    ): Result<Unit> = runCatchingLogged {
        listsRepository.removeBookFromList(
            listId = listId,
            bookId = bookId,
        )
    }
}
