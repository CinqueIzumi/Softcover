package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.UserBookStatus
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import timber.log.Timber

/**
 * Persists a drag-and-drop reorder for a built-in shelf. [prefixOrderedBookIds] is the top-down
 * order of the books spanned by the drag (positions `0..size-1`); books beyond that prefix are
 * intentionally left untouched so new entries fetched from the API still surface at the top of the
 * unsorted region rather than being pinned to the tail.
 */
class OnReorderShelfBooksAction(
    private val status: UserBookStatus,
    private val prefixOrderedBookIds: List<Int>,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        dependencies.reorderShelfBooksUseCase(
            status = status,
            prefixOrderedBookIds = prefixOrderedBookIds,
        ).onFailure {
            Timber.e("$it")
        }
    }
}
