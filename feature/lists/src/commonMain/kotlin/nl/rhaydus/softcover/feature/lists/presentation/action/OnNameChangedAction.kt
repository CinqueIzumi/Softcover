package nl.rhaydus.softcover.feature.lists.presentation.action

import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables
import nl.rhaydus.toad.ActionScope

internal class OnNameChangedAction(
    private val newName: String,
) : CreateListAction {
    override suspend fun execute(
        dependencies: CreateListDependencies,
        scope: ActionScope<CreateListUiState, CreateListEvent, LocalCreateListVariables>,
    ) {
        scope.setState { it.copy(name = newName) }
    }
}
