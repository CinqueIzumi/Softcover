package nl.rhaydus.softcover.core.network.helper

/** One page of rows plus the server's current row count for the whole query. */
class CountedPage<T>(
    val rows: List<T>,
    val total: Int,
)
