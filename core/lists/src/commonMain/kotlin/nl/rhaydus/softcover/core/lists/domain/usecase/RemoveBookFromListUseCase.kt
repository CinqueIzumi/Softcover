package nl.rhaydus.softcover.core.lists.domain.usecase

import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository

class RemoveBookFromListUseCase(
    private val listsRepository: ListsRepository,
) {
    suspend operator fun invoke(
        listId: Int,
        bookId: Int,
    ): Result<Unit> = runCatching {
        listsRepository.removeBookFromList(
            listId = listId,
            bookId = bookId,
        )
    }
}
