package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.softcover.core.domain.model.RefreshScope
import nl.rhaydus.softcover.core.presentation.error.onApiFailure
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnRefreshAction : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        val state: LibraryUiState = scope.currentState
        val selectedTab: LibraryTab? = state.visibleTabs.firstOrNull { it.id == state.selectedTabId }
        val refreshScope: RefreshScope = selectedTab.toRefreshScope()

        scope.setState { it.copy(isLoading = true) }

        dependencies.refreshLibraryUseCase(scope = refreshScope).onApiFailure()

        scope.setState { it.copy(isLoading = false) }
    }

    private fun LibraryTab?.toRefreshScope(): RefreshScope = when (this) {
        null,
        LibraryTab.All,
        -> RefreshScope.All

        is LibraryTab.Status -> RefreshScope.ByStatus(status = status)

        is LibraryTab.CustomList -> RefreshScope.ByList(listId = listId)
    }
}
