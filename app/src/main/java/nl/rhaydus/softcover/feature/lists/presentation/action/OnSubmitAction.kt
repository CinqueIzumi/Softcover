package nl.rhaydus.softcover.feature.lists.presentation.action

import nl.rhaydus.softcover.feature.lists.domain.exception.ListNameTakenException
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListCreatedEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListCreationFailedEvent
import nl.rhaydus.softcover.feature.lists.presentation.event.ListNameTakenEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables
import timber.log.Timber

class OnSubmitAction : CreateListAction {
    override suspend fun execute(
        dependencies: CreateListDependencies,
        scope: ActionScope<CreateListUiState, CreateListEvent, LocalCreateListVariables>,
    ) {
        val trimmed = scope.currentState.name.trim()

        if (trimmed.isEmpty() || scope.currentState.isSubmitting) return

        scope.setState { it.copy(isSubmitting = true) }

        runCatching {
            dependencies.createListUseCase(name = trimmed)
        }.onSuccess { created ->
            scope.setState { it.copy(isSubmitting = false) }

            scope.sendEvent(
                event = ListCreatedEvent(
                    listId = created.id,
                    name = created.name,
                ),
            )
        }.onFailure { error ->
            Timber.e(error, "Failed to create list $error")

            scope.setState { it.copy(isSubmitting = false) }

            val event: CreateListEvent = when (error) {
                is ListNameTakenException -> ListNameTakenEvent(name = error.name)
                else -> ListCreationFailedEvent()
            }

            scope.sendEvent(event = event)
        }
    }
}
