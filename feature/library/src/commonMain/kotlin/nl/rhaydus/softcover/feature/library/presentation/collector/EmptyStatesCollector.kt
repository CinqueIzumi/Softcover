package nl.rhaydus.softcover.feature.library.presentation.collector

import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.component.state.EmptyStateUiModel
import nl.rhaydus.softcover.core.presentation.model.LibraryTab
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.mapper.toEmptyStateUiModel
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class EmptyStatesCollector : LibraryCollector {
    override suspend fun onLaunch(
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
        dependencies: LibraryDependencies,
    ) {
        scope.state
            .map { state -> emptyStatesFor(visibleTabs = state.visibleTabs) }
            .distinctUntilChanged()
            .collectLatest { emptyStates ->
                scope.setState { it.copy(emptyStatesByTab = emptyStates) }
            }
    }
}

internal fun emptyStatesFor(visibleTabs: List<LibraryTab>): ImmutableMap<String, EmptyStateUiModel> =
    visibleTabs.associate { tab -> tab.id to tab.toEmptyStateUiModel() }.toImmutableMap()
