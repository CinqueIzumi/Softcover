package nl.rhaydus.softcover.core.profile.domain.model

import kotlinx.datetime.LocalDate

/** One day's reading by book, biggest first. */
data class ReadingDay(
    val date: LocalDate,
    val books: List<ReadingDayBook>,
)
