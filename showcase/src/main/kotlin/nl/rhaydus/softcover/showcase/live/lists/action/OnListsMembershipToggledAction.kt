package nl.rhaydus.softcover.showcase.live.lists.action

import nl.rhaydus.softcover.core.component.sheet.withModel
import nl.rhaydus.softcover.showcase.live.lists.event.ListsLiveEvent
import nl.rhaydus.softcover.showcase.live.lists.mapper.togglingMembership
import nl.rhaydus.softcover.showcase.live.lists.screenmodel.ListsLiveDependencies
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveLocalVariables
import nl.rhaydus.softcover.showcase.live.lists.state.ListsLiveUiState
import nl.rhaydus.toad.ActionScope

internal class OnListsMembershipToggledAction(val listId: Int) : ListsLiveAction {
    override suspend fun execute(
        dependencies: ListsLiveDependencies,
        scope: ActionScope<ListsLiveUiState, ListsLiveEvent, ListsLiveLocalVariables>,
    ) {
        scope.setState { it.copy(sheet = it.sheet.withModel { model -> model.togglingMembership(listId = listId) }) }
    }
}
