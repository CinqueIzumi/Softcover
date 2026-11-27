package nl.rhaydus.softcover.core.component.statistic

sealed interface StatTileVariant {
    /** The full-width lead tile, closing on a [caption] under a hairline. */
    data class Hero(val caption: String) : StatTileVariant

    /** The tall tile that stretches to its row, closing on a [caption] pinned to its foot. */
    data class Standard(val caption: String) : StatTileVariant

    /** The compact stacked tile, with a [unit] beside the number and an optional [caption] under it. */
    data class Compact(
        val unit: String,
        val caption: String? = null,
    ) : StatTileVariant
}
