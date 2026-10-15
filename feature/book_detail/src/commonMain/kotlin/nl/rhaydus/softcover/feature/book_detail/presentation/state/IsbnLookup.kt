package nl.rhaydus.softcover.feature.book_detail.presentation.state

/** The ISBN lookup to run once the edition sheet has finished hiding. */
internal data class IsbnLookup(
    val prefilledIsbn: String?,
)
