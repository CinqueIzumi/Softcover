package nl.rhaydus.softcover.core.profile.domain.model

/**
 * The reader's complete genre ranking. Each finished book counts with its top Hardcover genres plus
 * the genres the reader added themselves.
 *
 * The denominator of every [GenreSlice.fraction] is the finished books carrying at least one such
 * genre. Books with none are excluded rather than counted as genre-less zeroes, so how completely
 * the library is tagged does not move the shares.
 */
data class GenreBreakdown(
    val slices: List<GenreSlice> = emptyList(),
)
