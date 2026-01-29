package nl.rhaydus.softcover.feature.reading.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.reading.presentation.event.ReadingScreenEvent
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingLocalVariables
import nl.rhaydus.softcover.feature.reading.presentation.state.ReadingScreenUiState
import nl.rhaydus.softcover.feature.reading.presentation.viewmodel.ReadingScreenDependencies

data class OnUpdatePercentageProgressClickAction(
    val newPercentage: String,
) : ReadingAction {
    override suspend fun execute(
        dependencies: ReadingScreenDependencies,
        scope: ActionScope<ReadingScreenUiState, ReadingScreenEvent, ReadingLocalVariables>,
    ) {
        val bookToUpdate: Book = scope.currentState.bookToUpdate ?: return

        val newPercentageValue: Double = newPercentage.toDoubleOrNull() ?: 0.0

        val newPageValue: Int =
            ((newPercentageValue / 100) * (bookToUpdate.currentEdition.pages ?: 0)).toInt()

        dependencies.launch {
            dependencies.updateBookProgress(
                book = bookToUpdate,
                newPage = newPageValue,
                setLoading = { newLoading ->
                    scope.setState {
                        it.copy(isLoading = newLoading)
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