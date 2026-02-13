package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.screenmodel.ReadingScreenDependencies

data class OnUpdatePageProgressClickAction(val newPage: String) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        val bookToUpdate: Book = scope.currentState.bookToUpdate ?: return

        val newPageValue = newPage.toIntOrNull() ?: 0

        dependencies.launch {
            dependencies.updateBookProgress(
                book = bookToUpdate,
                newPage = newPageValue,
                setLoading = { newValue ->
                    scope.setState {
                        it.copy(isLoading = newValue)
                    }
                }
            )
        }

        scope.setState {
            it.copy(
                showProgressSheet = false,
                bookToUpdate = null,
            )
        }
    }
}