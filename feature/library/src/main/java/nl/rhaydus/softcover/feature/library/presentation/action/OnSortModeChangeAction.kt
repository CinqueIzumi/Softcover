package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import timber.log.Timber

internal class OnSortModeChangeAction(
    private val tabId: String,
    private val mode: LibrarySortMode,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val state = scope.currentState
        val currentMode = state.sortModeFor(tabId = tabId)
        val currentDirection = state.sortDirectionFor(tabId = tabId)

        // Tapping the active mode toggles direction; tapping a new mode resets to that mode's default.
        val nextDirection = if (mode == currentMode) {
            currentDirection.flipped()
        } else {
            mode.defaultDirection
        }

        // Close the menu first and let the persistence + collector roundtrip be the trigger for
        // the actual reorder. The menu close animation (~150 ms) gives the atomic DataStore write
        // and single-flow collector emission time to land, so the user perceives "tap → menu
        // closes → reveals sorted list" instead of seeing the grid reorganize behind the still-
        // open dropdown. Changing the sort also leaves rearrange mode — the positional order the
        // user was editing may no longer be what's shown.
        scope.setState {
            it.copy(
                isSortMenuExpanded = false,
                isRearranging = false,
            )
        }

        dependencies.setLibrarySortUseCase(
            tabId = tabId,
            mode = mode,
            direction = nextDirection,
        ).onFailure {
            Timber.e("$it")
        }
    }
}
