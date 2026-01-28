package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenDependencies
import timber.log.Timber

data class OnNewEditionSaveClickAction(val edition: BookEdition) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent>,
    ) {
        val userBookId = scope.currentState.bookToUpdate?.userBookId ?: return

        dependencies.launch {
            scope.setState { copy(isLoading = true) }

            dependencies.updateBookEditionUseCase(
                userBookId = userBookId,
                newEditionId = edition.id
            ).onFailure {
                Timber.e("-=- Something went wrong updating book edition! $it")
            }

            scope.setState { copy(isLoading = false) }
        }

        scope.setState { copy(showEditionSheet = false) }
    }
}