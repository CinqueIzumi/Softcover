package nl.rhaydus.softcover.showcase.live.lists.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.showcase.live.lists.event.ListsLiveEvent
import nl.rhaydus.softcover.showcase.live.lists.screenmodel.ListsLiveDependencies
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnListsDismissedAction : ListsLiveAction {
    override suspend fun execute(
        dependencies: ListsLiveDependencies,
        scope: ActionScope<ListsLiveUiState, ListsLiveEvent, ListsLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = SheetSession.Closed) }
    }
}
