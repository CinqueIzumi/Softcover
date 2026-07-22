package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.MonthCount

@Serializable
internal data class MonthCountEntity(
    val year: Int = 0,
    val month: Int = 0,
    val count: Int = 0,
)

internal fun MonthCountEntity.toModel(): MonthCount = MonthCount(
    year = year,
    month = month,
    count = count,
)

internal fun MonthCount.toEntity(): MonthCountEntity = MonthCountEntity(
    year = year,
    month = month,
    count = count,
)
