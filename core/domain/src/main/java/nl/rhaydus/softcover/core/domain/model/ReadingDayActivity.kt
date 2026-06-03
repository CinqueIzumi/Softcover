package nl.rhaydus.softcover.core.domain.model

import kotlinx.datetime.LocalDate

data class ReadingDayActivity(
    val date: LocalDate,
    val didRead: Boolean,
)
