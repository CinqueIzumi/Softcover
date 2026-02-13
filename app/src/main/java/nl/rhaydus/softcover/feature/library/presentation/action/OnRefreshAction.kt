package nl.rhaydus.softcover.feature.updated_library.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.updated_library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.updated_library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.updated_library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.updated_library.presentation.state.LibraryUiState
import timber.log.Timber

class OnRefreshAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { it.copy(isLoading = true) }

        dependencies.refreshUserBooksUseCase().onFailure {
            Timber.e("-=- $it")
        }

        scope.setState { it.copy(isLoading = false) }
    }
}