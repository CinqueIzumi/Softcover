package nl.rhaydus.softcover.core.component.bookcard

/**
 * A lazy-list key for this card, unique across kinds (`"book:12"`, `"edition:3"`, `"series:7"`). The
 * key itself is not Bundle-saveable on Android, so a lazy list cannot use it directly.
 */
val BookCardKey.lazyKey: String
    get() = when (this) {
        is BookCardKey.Book -> "book:$bookId"
        is BookCardKey.Edition -> "edition:$editionId"
        is BookCardKey.Series -> "series:$seriesId"
    }
