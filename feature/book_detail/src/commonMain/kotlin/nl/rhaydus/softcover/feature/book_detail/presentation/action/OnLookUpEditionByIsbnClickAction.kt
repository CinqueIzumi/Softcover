package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.component.sheet.requestClose
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.softcover.feature.book_detail.presentation.state.IsbnLookup
import nl.rhaydus.softcover.feature.book_detail.presentation.state.isbnPrefillFor
import nl.rhaydus.toad.ActionScope

/** Closes the edition sheet; the scanner opens once it has finished hiding. */
internal class OnLookUpEditionByIsbnClickAction : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        scope.setState {
            it.copy(
                editionSheetRequested = false,
                editionSheet = it.editionSheet.requestClose(
                    pending = IsbnLookup(prefilledIsbn = isbnPrefillFor(query = it.editionSearchQuery)),
                ),
            )
        }
    }
}
