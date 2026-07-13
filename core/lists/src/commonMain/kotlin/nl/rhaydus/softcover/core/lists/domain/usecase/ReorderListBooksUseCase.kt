package nl.rhaydus.softcover.core.lists.domain.usecase

import nl.rhaydus.common.runCatchingLogged
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository

/**
 * Persists a user-defined manual order for a contiguous slice of a custom list. The repository
 * applies the change locally first (optimistic) and then pushes it to Hardcover; this use case
 * just lifts the result into a `Result<Unit>` so the calling action can surface a toast on
 * server failure without crashing the screen model.
 */
class ReorderListBooksUseCase(
    private val listsRepository: ListsRepository,
) {
    suspend operator fun invoke(
        listId: Int,
        startPosition: Int,
        orderedListBookIds: List<Int>,
    ): Result<Unit> = runCatchingLogged {
        listsRepository.reorderListBooks(
            listId = listId,
            startPosition = startPosition,
            orderedListBookIds = orderedListBookIds,
        )
    }
}
