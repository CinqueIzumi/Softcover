package nl.rhaydus.softcover.core.profile.domain.model

/**
 * One of the user's top genres. [count] is how many of their genre-tagged books carry it, and
 * [fraction] is that count over [GenreBreakdown.taggedBookCount] — a share of *books*, so it reads
 * straight off as "N% of your books were X".
 *
 * Genres overlap (one book carries several), so these are not parts of a whole: the fractions across
 * slices do not sum to 1, and there is no remainder to add. Each is its own independent share, and
 * each can reach 1 on its own — a reader whose every book is tagged Fantasy sees Fantasy at 100%.
 */
data class GenreSlice(
    val name: String,
    val count: Int,
    val fraction: Double,
)
