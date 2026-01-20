package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenDependencies

data class OnNewEditionSaveClickAction(val edition: BookEdition) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent>,
    ) {
        val bookToUpdate = scope.currentState.bookToUpdate ?: return

        dependencies.launch {
            scope.setState { copy(isLoading = true) }

            dependencies.updateBookEditionUseCase(
                userBookId = bookToUpdate.userBookId,
                newEditionId = edition.id
            )

            scope.setState { copy(isLoading = false) }
        }

        scope.setState { copy(showEditionSheet = false) }
    }
}