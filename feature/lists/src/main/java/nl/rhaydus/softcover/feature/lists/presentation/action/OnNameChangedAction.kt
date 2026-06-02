package nl.rhaydus.softcover.feature.lists.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables

class OnNameChangedAction(
    private val newName: String,
) : CreateListAction {
    override suspend fun execute(
        dependencies: CreateListDependencies,
        scope: ActionScope<CreateListUiState, CreateListEvent, LocalCreateListVariables>,
    ) {
        scope.setState { it.copy(name = newName) }
    }
}
