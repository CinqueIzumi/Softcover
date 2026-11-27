package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.Quote

data class MostQuotedBook(
    val book: Book,
    val quoteCount: Int,
    val firstQuote: Quote,
)
