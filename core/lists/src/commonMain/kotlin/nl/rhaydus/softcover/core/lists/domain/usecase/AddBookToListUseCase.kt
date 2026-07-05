package nl.rhaydus.softcover.core.lists.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository

class AddBookToListUseCase(
    private val listsRepository: ListsRepository,
) {
    suspend operator fun invoke(
        listId: Int,
        bookId: Int,
        edition: BookEdition,
    ): Result<Unit> = runCatchingLogged {
        listsRepository.addBookToList(
            listId = listId,
            bookId = bookId,
            edition = edition,
        )
    }
}
