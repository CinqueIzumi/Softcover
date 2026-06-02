package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.designsystem.presentation.toad.ActionScope
import nl.rhaydus.softcover.core.designsystem.presentation.util.SnackBarManager
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import timber.log.Timber

/**
 * Updates the edition of the book already on the user's shelf to the scanned edition (the offer
 * surfaced by the scan-edition banner). The display is already showing the scanned edition; once the
 * cached user book reflects the new edition, [BookDetailUiState.showScanEditionUpdateBanner] turns
 * false on its own, so the scanned edition stays visible throughout without flicker.
 */
class OnUpdateToScannedEditionClickAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val userBook = scope.currentState.book?.userBook ?: return

        val scannedEditionId = scope.currentState.scannedEditionId ?: return

        if (scope.currentState.isUpdatingScannedEdition) return

        scope.setState { it.copy(isUpdatingScannedEdition = true) }

        dependencies.launch {
            dependencies.updateBookEditionUseCase(
                userBook = userBook,
                newEditionId = scannedEditionId,
            ).onFailure {
                Timber.e("Failed to update shelved edition to scanned edition $it")

                SnackBarManager.showSnackbar(title = "Couldn't update the edition — try again.")
            }

            scope.setState { it.copy(isUpdatingScannedEdition = false) }
        }
    }
}
