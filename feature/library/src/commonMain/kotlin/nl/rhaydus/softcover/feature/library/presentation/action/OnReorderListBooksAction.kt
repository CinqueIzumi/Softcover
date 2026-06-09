package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.designsystem.presentation.util.SnackBarManager
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState

/**
 * Persists a drag-and-drop reorder for a contiguous slice of a custom list. [orderedListBookIds]
 * is the new top-down order for visible indices `startPosition..startPosition + size - 1`; rows
 * outside the slice are left untouched so a shallow drag at the top of the list doesn't reassign
 * positions to books the user never moved.
 *
 * On server failure the local optimistic write stands and the next list refresh reconciles —
 * matching Step 2.7's "Hardcover is authoritative" spec. The toast surfaces the divergence so
 * the user knows the reorder hasn't synced yet.
 */
internal class OnReorderListBooksAction(
    private val listId: Int,
    private val startPosition: Int,
    private val orderedListBookIds: List<Int>,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        if (orderedListBookIds.isEmpty()) return

        val listName = scope.currentState.customLists
            .firstOrNull { it.id == listId }
            ?.name

        dependencies.reorderListBooksUseCase(
            listId = listId,
            startPosition = startPosition,
            orderedListBookIds = orderedListBookIds,
        ).onFailure { throwable ->
            AppLog.e(
                throwable,
                "Reorder failed for list $listId",
            )

            val suffix = listName?.let { " in \"$it\"" }.orEmpty()

            SnackBarManager.showSnackbar(
                title = "Couldn't save the new order$suffix — try again.",
            )
        }
    }
}
