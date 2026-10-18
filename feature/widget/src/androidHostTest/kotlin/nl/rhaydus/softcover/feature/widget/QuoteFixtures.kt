package nl.rhaydus.softcover.feature.widget

import nl.rhaydus.softcover.core.domain.model.PrivacySetting
import nl.rhaydus.softcover.core.domain.model.Quote
import nl.rhaydus.softcover.core.domain.model.QuotePosition
import kotlin.time.Instant

internal fun testQuote(
    bookId: Int = 1,
    text: String = "A line worth keeping",
    actionAtMillis: Long = 0L,
    localId: Long = 1L,
    remoteId: Long? = null,
    bookTitle: String? = null,
    editionTitle: String? = null,
    position: QuotePosition? = null,
): Quote = Quote(
    localId = localId,
    remoteId = remoteId,
    bookId = bookId,
    editionId = null,
    text = text,
    privacy = PrivacySetting.PUBLIC,
    actionAt = Instant.fromEpochMilliseconds(actionAtMillis),
    position = position,
    isPending = false,
    bookTitle = bookTitle,
    editionTitle = editionTitle,
)

internal fun testQuotes(count: Int): List<Quote> = (1..count).map { index ->
    testQuote(
        bookId = index,
        text = "Quote $index",
        localId = index.toLong(),
    )
}
