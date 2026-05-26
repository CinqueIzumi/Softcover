package nl.rhaydus.softcover.feature.settings.domain.model

enum class LibrarySortMode(
    val label: String,
    val defaultDirection: SortDirection,
) {
    DATE_ADDED(label = "Date added", defaultDirection = SortDirection.DESCENDING),
    DATE_FINISHED(label = "Date finished", defaultDirection = SortDirection.DESCENDING),
    TITLE(label = "Title", defaultDirection = SortDirection.ASCENDING),
    AUTHOR(label = "Author", defaultDirection = SortDirection.ASCENDING),
    RATING(label = "Rating", defaultDirection = SortDirection.DESCENDING),
    PROGRESS(label = "Progress", defaultDirection = SortDirection.DESCENDING),
    DEADLINE_URGENCY(label = "Deadline urgency", defaultDirection = SortDirection.ASCENDING),
    PAGE_COUNT(label = "Page count", defaultDirection = SortDirection.DESCENDING),
    // Defaults to ASCENDING so a "waiting for publication" list surfaces the soonest upcoming
    // release first; users who want newest-released first can toggle to DESCENDING.
    RELEASE_DATE(label = "Release date", defaultDirection = SortDirection.ASCENDING),
    // The user-defined drag-to-reorder mode for built-in shelves. Direction is fixed (ASC) — the
    // stored position itself encodes the order, so flipping ASC/DESC would only mirror the shelf.
    MANUAL(label = "Manual", defaultDirection = SortDirection.ASCENDING),
    // The user-defined drag-to-reorder mode for custom (ranked) lists. Distinct from MANUAL
    // because the underlying semantics differ: positions are server-owned on Hardcover via the
    // list_books.position column, only available when the list's `ranked` flag is true, and the
    // sort itself is the canonical view (other sorts are temporary lenses). Direction is fixed
    // (ASC) — the stored position itself encodes the order.
    ORDER(label = "Order", defaultDirection = SortDirection.ASCENDING),
    ;

    companion object {
        val Default: LibrarySortMode = DATE_ADDED
    }
}
