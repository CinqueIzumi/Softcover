package nl.rhaydus.softcover.core.database.model

/**
 * A `list_books` projection of just the id and manual-order position, read to snapshot the rows a
 * reorder rewrites so a rejected reorder can put them back.
 */
data class ListBookPositionRow(
    val listBookId: Int,
    val position: Int?,
)
