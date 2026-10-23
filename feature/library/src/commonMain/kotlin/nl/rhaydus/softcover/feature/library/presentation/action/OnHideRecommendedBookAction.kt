package nl.rhaydus.softcover.feature.library.presentation.action

import nl.rhaydus.common.AppLog
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.lists.domain.model.HiddenRecommendation
import nl.rhaydus.softcover.feature.library.presentation.event.LibraryEvent
import nl.rhaydus.softcover.feature.library.presentation.screenmodel.LibraryDependencies
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryLocalVariables
import nl.rhaydus.softcover.feature.library.presentation.state.LibraryUiState
import nl.rhaydus.toad.ActionScope

internal class OnHideRecommendedBookAction(
    val book: HiddenRecommendation,
) : LibraryAction {
    override suspend fun execute(
        dependencies: LibraryDependencies,
        scope: ActionScope<LibraryUiState, LibraryEvent, LibraryLocalVariables>,
    ) {
        dependencies.hideRecommendedBookUseCase(book = book)
            .onFailure {
                AppLog.e(
                    it,
                    "Failed to hide recommended book ${book.bookId}",
                )
            }
            .onSuccess {
                SnackBarManager.showSnackBar(
                    title = "\"${book.title ?: "Book"}\" won't be suggested again",
                    actionLabel = "Undo",
                    onActionClick = {
                        dependencies.launch {
                            dependencies.restoreRecommendedBookUseCase(bookId = book.bookId)
                                .onFailure {
                                    AppLog.e(
                                        it,
                                        "Failed to restore recommended book ${book.bookId}",
                                    )
                                }
                        }
                    },
                )
            }
    }
}
