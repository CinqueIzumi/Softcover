package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.designsystem.presentation.model.LibraryTab
import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

/**
 * Flips a custom list's `ranked` flag (the Hardcover `lists.ranked` column). When turning a list
 * on (`ranked = true`), the action also auto-switches the tab's sort mode to [LibrarySortMode.ORDER]
 * so the user sees the drag-to-reorder grid immediately — that is the implicit ask behind tapping
 * "Make this list ordered" in the sort dropdown.
 *
 * Both writes are optimistic: the repository flips the local cache, and we flip the local sort
 * setting via [LibraryDependencies.setLibrarySortUseCase]. On server failure we roll **both** back
 * and surface a snackbar; the repository already rolls back the `ranked` flag itself, but the sort
 * switch lives in DataStore and must be unwound here so the screen doesn't strand the user on an
 * ORDER tab that no longer has server backing.
 */
internal class OnSetListRankedAction(
    private val listId: Int,
    private val ranked: Boolean,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val state = scope.currentState
        val list = state.customLists.firstOrNull { it.id == listId }

        // Close the sort dropdown so the snackbar (on failure) and the auto-switched grid (on
        // success) aren't hidden behind it.
        scope.setState { it.copy(isSortMenuExpanded = false) }

        val tabId = LibraryTab.CustomList(
            listId = listId,
            listName = list?.name.orEmpty(),
        ).id

        val previousMode: LibrarySortMode = state.sortModeFor(tabId = tabId)
        val previousDirection: SortDirection = state.sortDirectionFor(tabId = tabId)

        // Auto-switch to ORDER only when turning ranked on. Turning ranked off leaves the user
        // on whatever sort they were using — ORDER would still be visible until the next
        // refresh trims it, but the dropdown's allow-list keeps that to a one-frame blip.
        if (ranked) {
            dependencies.setLibrarySortUseCase(
                tabId = tabId,
                mode = LibrarySortMode.ORDER,
                direction = LibrarySortMode.ORDER.defaultDirection,
            )
        }

        dependencies.setListRankedUseCase(
            listId = listId,
            ranked = ranked,
        ).onFailure { throwable ->
            AppLog.e(
                throwable,
                "Set list ranked=$ranked failed for list $listId",
            )

            if (ranked) {
                dependencies.setLibrarySortUseCase(
                    tabId = tabId,
                    mode = previousMode,
                    direction = previousDirection,
                )
            }

            val suffix = list?.name?.let { " for \"$it\"" }.orEmpty()
            val verb = if (ranked) "enable manual ordering" else "disable manual ordering"

            SnackBarManager.showSnackbar(
                title = "Couldn't $verb$suffix — try again.",
            )
        }
    }
}
