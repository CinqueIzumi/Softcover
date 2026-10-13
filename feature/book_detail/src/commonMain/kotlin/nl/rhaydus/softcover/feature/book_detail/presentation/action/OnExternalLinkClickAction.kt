package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.AMAZON_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.BOOKSHOP_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.OPEN_LIBRARY_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.searchIsbn
import nl.rhaydus.softcover.feature.book_detail.presentation.screenmodel.BookDetailDependencies
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailLocalVariables
import nl.rhaydus.softcover.feature.book_detail.presentation.state.BookDetailUiState
import nl.rhaydus.toad.ActionScope

internal class OnExternalLinkClickAction(val key: String) : BookDetailAction {
    override suspend fun execute(
        dependencies: BookDetailDependencies,
        scope: ActionScope<BookDetailUiState, BookDetailEvent, BookDetailLocalVariables>,
    ) {
        val isbn: String = scope.currentState.displayedEdition?.searchIsbn ?: return
        val url: String = when (key) {
            BOOKSHOP_KEY -> "https://bookshop.org/search?keywords=$isbn"
            AMAZON_KEY -> "https://www.amazon.com/s?k=$isbn"
            OPEN_LIBRARY_KEY -> "https://openlibrary.org/search?isbn=$isbn"
            else -> return
        }

        scope.sendEvent(event = BookDetailEvent.OpenExternalLinkEvent(url = url))
    }
}
