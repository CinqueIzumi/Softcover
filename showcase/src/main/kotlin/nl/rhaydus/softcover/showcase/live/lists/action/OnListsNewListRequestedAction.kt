package nl.rhaydus.softcover.showcase.live.lists.action

import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.showcase.live.lists.event.ListsLiveEvent
import nl.rhaydus.softcover.showcase.live.lists.screenmodel.ListsLiveDependencies
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnListsNewListRequestedAction : ListsLiveAction {
    override suspend fun execute(
        dependencies: ListsLiveDependencies,
        scope: ActionScope<ListsLiveUiState, ListsLiveEvent, ListsLiveLocalVariables>,
    ) {
        SnackBarManager.showSnackbar(title = "A new list would start here.")
    }
}
