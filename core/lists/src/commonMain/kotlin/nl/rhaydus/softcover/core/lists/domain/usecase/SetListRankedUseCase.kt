package nl.rhaydus.softcover.core.lists.domain.usecase

import nl.rhaydus.softcover.core.domain.result.runCatchingLogged
import nl.rhaydus.softcover.core.lists.domain.repository.ListsRepository

/**
 * Toggles a custom list's `ranked` flag. Wraps the repository call in a `Result<Unit>` so the
 * calling action can surface a snackbar on server failure without crashing the screen model.
 */
class SetListRankedUseCase(
    private val listsRepository: ListsRepository,
) {
    suspend operator fun invoke(
        listId: Int,
        ranked: Boolean,
    ): Result<Unit> = runCatchingLogged {
        listsRepository.setListRanked(
            listId = listId,
            ranked = ranked,
        )
    }
}
