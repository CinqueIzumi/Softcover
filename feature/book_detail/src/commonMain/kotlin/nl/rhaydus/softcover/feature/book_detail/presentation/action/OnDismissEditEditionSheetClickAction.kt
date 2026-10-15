package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.component.sheet.SheetSession
import nl.rhaydus.softcover.core.component.sheet.dismissed
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnDismissEditEditionSheetClickAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val lookup = scope.currentState.editionSheet.dismissed()

        scope.setState {
            it.copy(
                editionSheetRequested = false,
                editionSheet = SheetSession.Closed,
                editionSearchQuery = "",
                selectedEditionId = null,
            )
        }

        if (lookup != null) {
            scope.sendEvent(BookDetailEvent.OpenIsbnLookupEvent(prefilledIsbn = lookup.prefilledIsbn))
        }
    }
}
