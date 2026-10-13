package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.component.share.ShareCardSheetEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.withShareCapture
import nl.rhaydus.toad.ActionScope

/** Folds a share sheet's capture [event] into the held capture state; the sheet stays open until told to close. */
internal class OnShareCaptureEventAction(
    private val captureEvent: ShareCardSheetEvent,
) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                shareSheetRequested = it.shareSheetRequested && captureEvent != ShareCardSheetEvent.Shared,
            ).withShareCapture(it.shareCapture.reduce(captureEvent))
        }
    }
}
