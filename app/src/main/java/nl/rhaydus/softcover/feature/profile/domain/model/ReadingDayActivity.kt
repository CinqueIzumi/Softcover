package nl.rhaydus.softcover.feature.profile.domain.model

import java.time.LocalDate

data class ReadingDayActivity(
    val date: LocalDate,
    val didRead: Boolean,
)
