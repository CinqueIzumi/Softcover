package nl.rhaydus.softcover.feature.widget.presentation.util

internal const val TRENDING_PAGE_SIZE = 2
internal const val TRENDING_MAX_ITEMS = 10

/** Pages needed for [itemCount] books, counting only the top [TRENDING_MAX_ITEMS]. */
internal fun pageCount(itemCount: Int): Int {
    val shown = itemCount.coerceIn(0..TRENDING_MAX_ITEMS)

    return shown.ceilDiv(TRENDING_PAGE_SIZE)
}

/** [stored] pulled back into range, for a list that has shrunk since it was saved. */
internal fun resolveShownPage(
    stored: Int,
    pageCount: Int,
): Int = stored.coerceIn(0..lastPage(pageCount))

/** The page [step] places from the shown one, wrapping around the list. */
internal fun nextPage(
    stored: Int,
    pageCount: Int,
    step: Int,
): Int {
    if (pageCount <= 0) return 0

    val shown = resolveShownPage(
        stored = stored,
        pageCount = pageCount,
    )

    return (shown + step).mod(pageCount)
}

private fun lastPage(pageCount: Int): Int = (pageCount - 1).coerceAtLeast(0)

private fun Int.ceilDiv(divisor: Int): Int = (this + divisor - 1) / divisor
