package nl.rhaydus.softcover.core.network.helper

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
 */
suspend fun <T> fetchAllPages(
    pageSize: Int = 100,
    maxPages: Int = 100,
    fetchPage: suspend (limit: Int, offset: Int) -> List<T>,
): List<T> {
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
        if (page.size < pageSize) break
    }

    return rows
}
