package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.AuthorDemographics

@Serializable
internal data class AuthorDemographicsEntity(
    val genderSlices: List<GenderSliceEntity> = emptyList(),
    val knownGenderCount: Int = 0,
    val unknownGenderCount: Int = 0,
    val bipocBreakdown: DemographicBreakdownEntity = DemographicBreakdownEntity(),
    val lgbtqBreakdown: DemographicBreakdownEntity = DemographicBreakdownEntity(),
)

internal fun AuthorDemographicsEntity.toModel(): AuthorDemographics = AuthorDemographics(
    genderSlices = genderSlices.map { it.toModel() },
    knownGenderCount = knownGenderCount,
    unknownGenderCount = unknownGenderCount,
    bipocBreakdown = bipocBreakdown.toModel(),
    lgbtqBreakdown = lgbtqBreakdown.toModel(),
)

internal fun AuthorDemographics.toEntity(): AuthorDemographicsEntity = AuthorDemographicsEntity(
    genderSlices = genderSlices.map { it.toEntity() },
    knownGenderCount = knownGenderCount,
    unknownGenderCount = unknownGenderCount,
    bipocBreakdown = bipocBreakdown.toEntity(),
    lgbtqBreakdown = lgbtqBreakdown.toEntity(),
)
