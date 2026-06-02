package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber

class InitializeBookWithIdAction(
    val id: Int,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        dependencies.launch {
            val result = dependencies
                .fetchBookByIdUseCase(id = id)
                .onFailure { Timber.e("$it") }
                .getOrNull()

            scope.setState { state ->
                // Seed userBook/userBookRead from initialCover so currentEdition resolves
                // to the user's edition immediately, avoiding a one-frame flash to the
                // remote book's defaultEdition before UserBooksFlowCollector overlays.
                val merged = result?.let { fetched ->
                    state.initialCover?.let { initial ->
                        fetched.copy(
                            userBook = fetched.userBook ?: initial.userBook,
                            userBookRead = fetched.userBookRead ?: initial.userBookRead,
                        )
                    } ?: fetched
                }

                state.copy(
                    book = merged,
                    editions = merged?.editions ?: emptyList(),
                    loadingBookDetails = false,
                )
            }

            result?.let { book ->
                scope.setLocalVariables { locals ->
                    locals.copy(editionsLoadedForBookId = book.id)
                }
            }
        }
    }
}
