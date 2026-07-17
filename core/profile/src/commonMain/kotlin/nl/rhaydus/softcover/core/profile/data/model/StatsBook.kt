package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.datetime.LocalDate

// A lean, plain-Kotlin projection of one finished book, extracted from the paginated
// GetReadUserBooksForStats response so the aggregation transforms (year/month/genre/rating
// bucketing) are pure functions over simple data rather than the generated GraphQL types.
internal data class StatsBook(
    val rating: Double?,
    val finishDate: LocalDate?,
    val pages: Int,
    val genreNames: List<String>,
)
