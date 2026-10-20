package nl.rhaydus.softcover.feature.explore.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import nl.rhaydus.softcover.core.book.domain.repository.BooksRepository
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository

/**
 * Ids of the books the reader already owns: on a shelf ([booksRepository]) or on any list
 * ([listsRepository]). Re-emits as either changes, so a rail filtering by it drops a book the moment it is
 * shelved or listed.
 */
class ObserveOwnedBookIdsUseCase(
    private val booksRepository: BooksRepository,
    private val listsRepository: ListsRepository,
) {
    operator fun invoke(): Flow<Set<Int>> = combine(
        booksRepository.books,
        listsRepository.allUserLists,
    ) { allUserBooks, allUserLists ->
        allUserBooks.map { it.id }.toSet() +
            allUserLists.flatMap { list -> list.books }.map { it.bookId }.toSet()
    }
}
