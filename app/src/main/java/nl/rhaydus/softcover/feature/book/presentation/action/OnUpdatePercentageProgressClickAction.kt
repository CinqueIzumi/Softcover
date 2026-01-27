package nl.rhaydus.softcover.feature.book.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.viewmodel.BookDetailDependencies

// TODO: This has quite a bit of duplicate code...
data class OnUpdatePercentageProgressClickAction(
    val newPercentage: String,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent>,
    ) {
        val bookToUpdate: Book = scope.currentState.book ?: return

        val newPercentageValue: Double = newPercentage.toDoubleOrNull() ?: 0.0

        val newPageValue: Int =
            ((newPercentageValue / 100) * (bookToUpdate.currentEdition.pages ?: 0)).toInt()

        dependencies.launch {
            dependencies.updateBookProgress(
                book = bookToUpdate,
                newPage = newPageValue,
                setLoading = { newLoading ->
                    scope.setState { copy(loading = newLoading) }
                }
            )
        }

        scope.setState {
            copy(showUpdateProgressSheet = false)
        }
    }
}