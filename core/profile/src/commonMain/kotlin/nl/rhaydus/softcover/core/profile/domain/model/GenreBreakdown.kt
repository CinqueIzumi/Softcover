package nl.rhaydus.softcover.core.profile.domain.model

/**
 * The reader's top genres, together with the denominator every [GenreSlice.fraction] is taken over:
 * [taggedBookCount], the number of finished books carrying at least one genre tag.
 *
 * Books with no genre data are excluded from that denominator rather than counted as genre-less
 * zeroes. Dividing by every finished book would blend two unrelated things into one number — how
 * much of a genre the reader actually reads, and how completely their library happens to have been
 * tagged — and nothing on screen would let the reader tell which one moved it.
 *
 * The slices and their denominator travel together because neither means anything alone: a slice's
 * fraction is unreadable without knowing what it was divided by, and the count is what the section's
 * footnote names.
 */
data class GenreBreakdown(
    val slices: List<GenreSlice> = emptyList(),
    val taggedBookCount: Int = 0,
)
