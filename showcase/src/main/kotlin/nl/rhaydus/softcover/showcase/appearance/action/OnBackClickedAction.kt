package nl.rhaydus.softcover.showcase.appearance.action

import nl.rhaydus.toad.ActionDependencies
import nl.rhaydus.toad.ActionScope
import nl.rhaydus.toad.LocalVariables
import nl.rhaydus.toad.UiAction
import nl.rhaydus.toad.UiEvent
import nl.rhaydus.toad.UiState

/** [backEvent] is the screen's own event that its event collector turns into a pop. */
internal class OnBackClickedAction<
    D : ActionDependencies,
    S : UiState,
    E : UiEvent,
    V : LocalVariables,
    >(
    private val backEvent: E,
) : UiAction<D, S, E, V> {
    override suspend fun execute(
        dependencies: D,
        scope: ActionScope<S, E, V>,
    ) {
        scope.sendEvent(event = backEvent)
    }
}
