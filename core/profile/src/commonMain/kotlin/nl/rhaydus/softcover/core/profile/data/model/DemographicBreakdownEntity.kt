package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.DemographicBreakdown

@Serializable
internal data class DemographicBreakdownEntity(
    val yesCount: Int = 0,
    val noCount: Int = 0,
    val unknownCount: Int = 0,
)

internal fun DemographicBreakdownEntity.toModel(): DemographicBreakdown = DemographicBreakdown(
    yesCount = yesCount,
    noCount = noCount,
    unknownCount = unknownCount,
)

internal fun DemographicBreakdown.toEntity(): DemographicBreakdownEntity = DemographicBreakdownEntity(
    yesCount = yesCount,
    noCount = noCount,
    unknownCount = unknownCount,
)
