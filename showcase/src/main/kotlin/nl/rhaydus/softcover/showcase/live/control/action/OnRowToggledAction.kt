package nl.rhaydus.softcover.showcase.live.control.action

import kotlinx.collections.immutable.toImmutableList
import nl.rhaydus.softcover.showcase.live.control.event.ControlLiveEvent
import nl.rhaydus.softcover.showcase.live.control.screenmodel.ControlLiveDependencies
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.control.state.ControlLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnRowToggledAction(
    val key: String,
    val checked: Boolean,
) : ControlLiveAction {
    override suspend fun execute(
        dependencies: ControlLiveDependencies,
        scope: ActionScope<ControlLiveUiState, ControlLiveEvent, ControlLiveLocalVariables>,
    ) {
        scope.setState { state ->
            state.copy(
                toggleRows = state.toggleRows
                    .map { if (it.key == key) it.copy(checked = checked) else it }
                    .toImmutableList(),
            )
        }
    }
}
