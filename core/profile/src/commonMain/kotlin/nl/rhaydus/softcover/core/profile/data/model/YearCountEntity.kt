package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.YearCount

@Serializable
internal data class YearCountEntity(
    val year: Int = 0,
    val count: Int = 0,
)

internal fun YearCountEntity.toModel(): YearCount = YearCount(
    year = year,
    count = count,
)

internal fun YearCount.toEntity(): YearCountEntity = YearCountEntity(
    year = year,
    count = count,
)
