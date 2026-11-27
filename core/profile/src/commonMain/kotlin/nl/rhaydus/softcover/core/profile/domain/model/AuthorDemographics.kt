package nl.rhaydus.softcover.core.profile.domain.model

data class AuthorDemographics(
    val genderSlices: List<GenderSlice> = emptyList(),
    val knownGenderCount: Int = 0,
    val unknownGenderCount: Int = 0,
    val bipocBreakdown: DemographicBreakdown = DemographicBreakdown(),
    val lgbtqBreakdown: DemographicBreakdown = DemographicBreakdown(),
) {
    fun excludingBooksWithoutDetail(): AuthorDemographics = AuthorDemographics(
        genderSlices = genderSlices.excludingUnknownGender(knownGenderCount = knownGenderCount),
        knownGenderCount = knownGenderCount,
        unknownGenderCount = 0,
        bipocBreakdown = bipocBreakdown.copy(unknownCount = 0),
        lgbtqBreakdown = lgbtqBreakdown.copy(unknownCount = 0),
    )
}

private fun List<GenderSlice>.excludingUnknownGender(knownGenderCount: Int): List<GenderSlice> {
    if (knownGenderCount == 0) return emptyList()

    return filter { it.gender != AuthorshipGender.Unknown }
        .map { slice -> slice.copy(fraction = slice.count.toDouble() / knownGenderCount) }
}
