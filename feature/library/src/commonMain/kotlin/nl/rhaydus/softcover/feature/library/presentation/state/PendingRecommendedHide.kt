package nl.rhaydus.softcover.feature.library.presentation.state

/** The "Not for me" choice on [bookId]'s recommended-rail menu: the sheet row's key and the intent it hands back. */
internal data class PendingRecommendedHide(
    val bookId: Int,
)
