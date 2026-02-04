package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import timber.log.Timber

class OnRefreshAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { it.copy(isLoading = true) }

        dependencies.initializeUserBooksUseCase().onFailure {
            Timber.e("-=- $it")
        }

        scope.setState { it.copy(isLoading = false) }
    }
}