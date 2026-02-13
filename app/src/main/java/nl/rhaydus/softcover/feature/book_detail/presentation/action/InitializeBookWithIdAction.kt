package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.presentation.toad.ActionScope
import timber.log.Timber

class InitializeBookWithIdAction(
    val id: Int,
) : nl.rhaydus.softcover.feature.book_detail.presentation.action.BookDetailAction {
    private lateinit var dependencies: nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
    private lateinit var scope: ActionScope<nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState, nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent, nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables>

    override suspend fun execute(
        dependencies: nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies,
        scope: ActionScope<nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState, nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent, nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables>,
    ) {
        this.dependencies = dependencies
        this.scope = scope

        handleAction()
    }

    private fun handleAction() {
        dependencies.launch {
            val result = dependencies
                .fetchBookByIdUseCase(id = id)
                .onFailure { Timber.e("-=- $it") }
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