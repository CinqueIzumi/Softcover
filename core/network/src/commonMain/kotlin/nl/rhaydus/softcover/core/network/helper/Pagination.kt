package nl.rhaydus.softcover.core.network.helper

import nl.rhaydus.softcover.core.domain.exception.UnexpectedApiException

/**
 * Pages through [fetchPage] until a short page signals the end, accumulating every row into one
 * list. [fetchPage] must return the raw page rows straight from the query — the termination check
 * compares the page's size against [pageSize], so mapping, filtering, or aggregating inside
 * [fetchPage] can make a full page look short and end paging early. Do that transformation on the
 * returned list instead.
 *
 * [pageSize] is a conservative page count comfortably under the server's per-request row cap (the
 * live Hardcover API cuts unbounded list queries off in the high hundreds); advancing the offset by
 * the page's actual row count keeps paging correct regardless of the cap's exact value. [maxPages]
 * is a runaway guard so a pathological account can never turn this into an unbounded loop.
 *
 * Reaching [maxPages] returns the rows collected so far, which may be a truncated list. A caller whose
 * result feeds a delete, prune or replace-all must use [fetchAllPagesOrThrow] instead.
 */
suspend fun <T> fetchAllPages(
    pageSize: Int = 100,
    maxPages: Int = 100,
    fetchPage: suspend (limit: Int, offset: Int) -> List<T>,
): List<T> = collectPages(
    pageSize = pageSize,
    maxPages = maxPages,
    fetchPage = fetchPage,
).rows

/**
 * [fetchAllPages] with a complete-or-throw contract: if [maxPages] full pages are fetched without a
 * short page confirming the end, the list may be incomplete, so this throws [UnexpectedApiException]
 * rather than returning it.
 */
suspend fun <T> fetchAllPagesOrThrow(
    pageSize: Int,
    maxPages: Int,
    fetchPage: suspend (limit: Int, offset: Int) -> List<T>,
): List<T> {
    val collected = collectPages(
        pageSize = pageSize,
        maxPages = maxPages,
        fetchPage = fetchPage,
    )

    if (collected.endConfirmed.not()) {
        throw UnexpectedApiException(
            message = "Paging stopped at $maxPages pages of $pageSize rows without reaching the end",
        )
    }

    return collected.rows
}

/**
 * Pages through [fetchPage] until as many rows have arrived as the server reports in [CountedPage.total],
 * so the bound comes from the data rather than a fixed page ceiling. The count must come from the same
 * request as the page (an aggregate under the same root), which keeps it one billed request.
 *
 * The latest reported total wins: a row added mid-fetch raises it and one more page is fetched, a row
 * removed lowers it and paging ends sooner. Each page advances the offset by its actual row count, and an
 * empty page ends paging, so the loop always terminates.
 *
 * Rows are counted by unique [key], and the deduped rows are returned. Throws [UnexpectedApiException] when
 * a short or empty page ends paging with fewer unique rows than the total, or when a full page adds no new
 * key (a server ignoring the offset), so a caller feeding a prune or replace-all never sees an incomplete
 * list.
 */
suspend fun <T> fetchAllPagesCounted(
    pageSize: Int,
    key: (T) -> Any,
    fetchPage: suspend (limit: Int, offset: Int) -> CountedPage<T>,
): List<T> {
    val unique = LinkedHashMap<Any, T>()
    var offset = 0
    var total = Int.MAX_VALUE

    while (unique.size < total) {
        val page = fetchPage(
            pageSize,
            offset,
        )

        val uniqueBefore = unique.size

        for (row in page.rows) {
            unique.getOrPut(key(row)) { row }
        }

        offset += page.rows.size
        total = page.total

        if (unique.size >= total) break

        if (page.rows.size < pageSize) {
            throw UnexpectedApiException(
                message = "Paging ended at ${unique.size} of $total rows",
            )
        }

        if (unique.size == uniqueBefore) {
            throw UnexpectedApiException(
                message = "Paging made no progress at ${unique.size} of $total rows",
            )
        }
    }

    return unique.values.toList()
}

private class CollectedPages<T>(
    val rows: List<T>,
    val endConfirmed: Boolean,
)

private suspend fun <T> collectPages(
    pageSize: Int,
    maxPages: Int,
    fetchPage: suspend (limit: Int, offset: Int) -> List<T>,
): CollectedPages<T> {
    val rows = mutableListOf<T>()
    var offset = 0
    var pages = 0

    while (pages < maxPages) {
        val page = fetchPage(
            pageSize,
            offset,
        )

        rows += page

        offset += page.size
        pages++

        // A partial page (including empty) means this was the last one - a full page always
        // requests one more to confirm, but a short page already proves there's nothing left.
        if (page.size < pageSize) return CollectedPages(
            rows = rows,
            endConfirmed = true,
        )
    }

    return CollectedPages(
        rows = rows,
        endConfirmed = false,
    )
}
