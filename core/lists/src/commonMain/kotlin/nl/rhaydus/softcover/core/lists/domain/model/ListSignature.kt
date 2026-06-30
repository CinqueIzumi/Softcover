package nl.rhaydus.softcover.core.lists.domain.model

/**
 * A cheap freshness fingerprint for a single list, fetched without its `list_books` payload. The
 * [signature] folds the list's `updated_at`, its book count, and the newest `list_books.updated_at`
 * into one string, so a content add/remove/reorder changes it even when the list's own `updated_at`
 * does not. Used to decide whether a list's contents must be deep-fetched again.
 */
data class ListSignature(
    val listId: Int,
    val signature: String,
)
