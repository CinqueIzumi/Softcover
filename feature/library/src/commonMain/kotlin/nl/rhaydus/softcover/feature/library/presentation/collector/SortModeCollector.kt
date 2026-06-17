package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.coroutines.flow.collectLatest
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class SortModeCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        dependencies.getLibrarySortSettingsAsFlowUseCase().collectLatest { settings ->
            scope.setState {
                it.copy(
                    sortModeByTab = settings.mapValues { (_, s) -> s.mode },
                    sortDirectionByTab = settings.mapValues { (_, s) -> s.direction },
                )
            }
        }
    }
}
