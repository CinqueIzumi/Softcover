package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.presentation.toad.ActionScope

data class OnUpdatePercentageProgressClickAction(
    val newPercentage: String,
) : nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction {
    override suspend fun execute(
        dependencies: nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies,
        scope: ActionScope<nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState, nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent, nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables>,
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
                    scope.setState { it.copy(loading = newLoading) }
                }
            )
        }

        scope.setState {
            it.copy(showUpdateProgressSheet = false)
        }
    }
}