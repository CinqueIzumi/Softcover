package nl.rhaydus.softcover.feature.book_detail.presentation.action

import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.event.BookDetailEvent
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.AMAZON_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.BOOKSHOP_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.KOBO_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.OPEN_LIBRARY_KEY
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.koboQuery
import nl.rhaydus.softcover.feature.book_detail.presentation.mapper.koboSearchUrl
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
        val edition: BookEdition = scope.currentState.displayedEdition ?: return
        val isbn: String? = edition.searchIsbn
        val url: String = when (key) {
            BOOKSHOP_KEY -> isbn?.let { "https://bookshop.org/search?keywords=$it" }
            AMAZON_KEY -> isbn?.let { "https://www.amazon.com/s?k=$it" }
            KOBO_KEY -> edition.koboQuery?.let { koboSearchUrl(query = it) }
            OPEN_LIBRARY_KEY -> isbn?.let { "https://openlibrary.org/search?isbn=$it" }
            else -> null
        } ?: return

        scope.sendEvent(event = BookDetailEvent.OpenExternalLinkEvent(url = url))
    }
}
