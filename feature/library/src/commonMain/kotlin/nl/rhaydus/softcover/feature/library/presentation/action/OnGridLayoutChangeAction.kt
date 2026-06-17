package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.domain.logging.AppLog
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnGridLayoutChangeAction(
    private val newLayout: LibraryGridLayout,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        scope.setState { it.copy(isLayoutMenuExpanded = false) }

        dependencies.setLibraryGridLayoutUseCase(newLayout = newLayout).onFailure {
            AppLog.e("$it")
        }
    }
}
