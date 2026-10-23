package nl.rhaydus.softcover.showcase.live.selection.action

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.showcase.live.selection.event.SelectionLiveEvent
import nl.rhaydus.softcover.showcase.live.selection.screenmodel.SelectionLiveDependencies
import nl.rhaydus.softcover.showcase.live.selection.state.SelectionLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.selection.state.SelectionLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnRowToggledAction(val key: String) : SelectionLiveAction {
    override suspend fun execute(
        dependencies: SelectionLiveDependencies,
        scope: ActionScope<SelectionLiveUiState, SelectionLiveEvent, SelectionLiveLocalVariables>,
    ) {
        scope.setState { state ->
            state.copy(
                shelves = state.shelves
                    .map { if (it.key == key) it.copy(selected = it.selected.not()) else it }
                    .toImmutableList(),
                formats = state.formats
                    .map { if (it.key == key) it.copy(selected = it.selected.not()) else it }
                    .toImmutableList(),
            )
        }
    }
}
