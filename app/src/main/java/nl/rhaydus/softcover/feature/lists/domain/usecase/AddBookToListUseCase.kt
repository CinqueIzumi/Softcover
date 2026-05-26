package nl.rhaydus.softcover.feature.lists.domain.usecase

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.lists.domain.repository.ListsRepository

class AddBookToListUseCase(
    private val listsRepository: ListsRepository,
) {
    suspend operator fun invoke(
        listId: Int,
        bookId: Int,
        edition: BookEdition,
    ): Result<Unit> = runCatching {
        listsRepository.addBookToList(
            listId = listId,
            bookId = bookId,
            edition = edition,
        )
    }
}
