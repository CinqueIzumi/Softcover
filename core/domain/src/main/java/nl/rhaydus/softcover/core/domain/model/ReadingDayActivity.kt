package nl.rhaydus.softcover.core.domain.model

import java.time.LocalDate

data class ReadingDayActivity(
    val date: LocalDate,
    val didRead: Boolean,
)
