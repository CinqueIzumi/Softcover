package nl.rhaydus.softcover.core.presentation.removal

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarResult
import kotlinx.coroutines.launch
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.designsystem.util.SnackBarManager
import nl.rhaydus.softcover.core.book.domain.model.StagedBookRemoval
import nl.rhaydus.softcover.core.book.domain.usecase.CommitBookRemovalUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.StageBookRemovalUseCase
import nl.rhaydus.softcover.core.book.domain.usecase.UndoBookRemovalUseCase
import nl.rhaydus.softcover.core.domain.model.ApplicationScope
import nl.rhaydus.softcover.core.domain.model.Book

/**
 * Removes books from the library behind an Undo snackbar. The books leave the local cache at once;
 * the remote delete is deferred until the snackbar closes, so Undo restores status, progress,
 * rating and review that a delete-then-re-add could not.
 *
 * The window and the commit run on [ApplicationScope] so leaving the screen never strands a
 * staged removal. A process kill inside the window loses the remote delete, and the book returns
 * on the next sync.
 */
class UndoableBookRemover internal constructor(
    private val stageBookRemovalUseCase: StageBookRemovalUseCase,
    private val undoBookRemovalUseCase: UndoBookRemovalUseCase,
    private val commitBookRemovalUseCase: CommitBookRemovalUseCase,
    private val applicationScope: ApplicationScope,
    private val appDispatchers: AppDispatchers,
) {
    /** Stages the removal and opens the undo window. The returned [Result] covers staging only. */
    suspend fun remove(books: List<Book>): Result<Unit> {
        if (books.isEmpty()) return Result.success(Unit)

        return stageBookRemovalUseCase(books = books)
            .onFailure { SnackBarManager.showSnackbar(title = books.failureMessage()) }
            .map { staged ->
                if (staged.books.isEmpty()) return@map

                applicationScope.scope.launch(appDispatchers.main) {
                    awaitWindow(staged = staged)
                }
            }
    }

    private suspend fun awaitWindow(staged: StagedBookRemoval) {
        val result = SnackBarManager.snackBarState.value.showSnackbar(
            message = staged.books.removedMessage(),
            actionLabel = "Undo",
            duration = SnackbarDuration.Short,
        )

        when (result) {
            SnackbarResult.ActionPerformed -> undoBookRemovalUseCase(staged = staged)

            SnackbarResult.Dismissed -> commitBookRemovalUseCase(staged = staged).onSuccess { failed ->
                if (failed.isEmpty()) return@onSuccess

                SnackBarManager.showSnackbar(title = failed.failureMessage())
            }
        }
    }

    private fun List<Book>.failureMessage(): String {
        val noun = if (size == 1) "book" else "books"

        return "Couldn't remove $size $noun. Try again."
    }

    private fun List<Book>.removedMessage(): String = when (size) {
        1 -> "Removed ${first().title}"
        else -> "Removed $size books"
    }
}
