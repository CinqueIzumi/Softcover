package nl.rhaydus.softcover.core.domain.model

import kotlin.time.Instant

data class Quote(
    val localId: Long,
    /** `reading_journals.id`; `null` until the insert reaches Hardcover. */
    val remoteId: Long?,
    val bookId: Int,
    val editionId: Int?,
    val text: String,
    val privacy: PrivacySetting,
    val actionAt: Instant,
    val position: QuotePosition?,
    /** The insert has not been confirmed by Hardcover yet. */
    val isPending: Boolean,
    val bookTitle: String? = null,
)
