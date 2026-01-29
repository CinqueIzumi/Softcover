package nl.rhaydus.softcover.feature.book.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book.presentation.viewmodel.BookDetailDependencies

class InitializeBookWithIdAction(
    val id: Int,
) : BookDetailAction {
    private lateinit var dependencies: BookDetailDependencies
    private lateinit var scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>

    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        this.dependencies = dependencies
        this.scope = scope

        handleAction()
    }

    private fun handleAction() {
        dependencies.launch {
            val result = dependencies
                .fetchBookByIdUseCase(id = id)
                .getOrNull()

            scope.setState {
                it.copy(
                    book = result,
                    loading = false,
                )
            }
        }
    }
}