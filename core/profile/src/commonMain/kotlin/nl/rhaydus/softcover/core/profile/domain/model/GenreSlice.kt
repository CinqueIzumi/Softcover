package nl.rhaydus.softcover.core.profile.domain.model

/**
 * One of the user's top genres. [fraction] is a share of genre *assignments*, not of books — a book
 * carries several genres — so the fractions of the slices shown do not sum to 1.
 */
data class GenreSlice(
    val name: String,
    val count: Int,
    val fraction: Double,
)
