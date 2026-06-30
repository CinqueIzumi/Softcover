package nl.rhaydus.softcover.core.database.model

/**
 * A `book_lists` projection of just the id and cached freshness signature, read to decide which
 * lists need their `list_books` deep-fetched again without loading the full rows.
 */
data class ListSignatureRow(
    val id: Int,
    val signature: String?,
)
