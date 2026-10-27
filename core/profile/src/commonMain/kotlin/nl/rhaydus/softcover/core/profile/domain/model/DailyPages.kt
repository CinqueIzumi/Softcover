package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalDate

/** Pages read per day of [year]; days with no pages are absent. */
data class DailyPages(
    val year: Int,
    val pages: Map<LocalDate, Int>,
)
