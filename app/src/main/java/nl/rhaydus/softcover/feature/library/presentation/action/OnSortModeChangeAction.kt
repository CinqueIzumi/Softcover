package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import timber.log.Timber

class OnSortModeChangeAction(
    private val tabId: String,
    private val mode: LibrarySortMode,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { it.copy(isSortMenuExpanded = false) }

        dependencies.setLibrarySortModeUseCase(
            tabId = tabId,
            mode = mode,
        ).onFailure {
            Timber.e("-=- $it")
        }
    }
}
