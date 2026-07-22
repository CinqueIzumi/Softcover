package nl.rhaydus.softcover.core.personal.domain.model

import kotlinx.datetime.LocalDate

data class ReadingJournalEntry(
    val date: LocalDate,
    val pages: Int?,
    val seconds: Int?,
)
