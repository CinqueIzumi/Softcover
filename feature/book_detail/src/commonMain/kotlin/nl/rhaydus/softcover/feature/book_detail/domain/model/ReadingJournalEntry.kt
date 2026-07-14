package nl.rhaydus.softcover.feature.book_detail.domain.model

import kotlinx.datetime.LocalDate

data class ReadingJournalEntry(
    val date: LocalDate,
    val pages: Int?,
    val seconds: Int?,
)
