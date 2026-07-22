package nl.rhaydus.softcover.core.profile.data.model

import nl.rhaydus.softcover.core.domain.model.Gender

// A lean, plain-Kotlin projection of one contribution's author, extracted from the paginated
// GetReadUserBooksForStats response - mirrors StatsBook. Only the fields the author-demographics
// aggregation needs; name is intentionally omitted to keep this per-book, paginated payload lean.
internal data class StatsAuthor(
    val id: Int,
    val isBipoc: Boolean?,
    val isLgbtq: Boolean?,
    val gender: Gender,
)
