package nl.rhaydus.softcover.feature.lists.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.softcover.core.lists.domain.exception.ListNameTakenException
import nl.rhaydus.softcover.feature.lists.presentation.event.CreateListEvent
import nl.rhaydus.softcover.feature.lists.presentation.screenmodel.CreateListDependencies
import nl.rhaydus.softcover.feature.lists.presentation.state.CreateListUiState
import nl.rhaydus.softcover.feature.lists.presentation.state.LocalCreateListVariables
import nl.rhaydus.toad.ActionScope

internal class OnSubmitAction : CreateListAction {
    override suspend fun execute(
        dependencies: CreateListDependencies,
        scope: ActionScope<CreateListUiState, CreateListEvent, LocalCreateListVariables>,
    ) {
        val trimmed = scope.currentState.name.trim()

        if (trimmed.isEmpty() || scope.currentState.isSubmitting) return

        val privacy = scope.currentState.privacy

        scope.setState { it.withSubmitting(submitting = true) }

        runCatching {
            dependencies.createListUseCase(
                name = trimmed,
                privacy = privacy,
            )
        }.onSuccess { created ->
            scope.setState { it.withSubmitting(submitting = false) }

            scope.sendEvent(
                event = CreateListEvent.ListCreatedEvent(
                    listId = created.id,
                    name = created.name,
                ),
            )
        }.onFailure { error ->
            AppLog.e(
                error,
                "Failed to create list $error",
            )

            scope.setState { it.withSubmitting(submitting = false) }

            val event: CreateListEvent = when (error) {
                is ListNameTakenException -> CreateListEvent.ListNameTakenEvent(name = error.name)
                else -> CreateListEvent.ListCreationFailedEvent()
            }

            scope.sendEvent(event = event)
        }
    }
}
