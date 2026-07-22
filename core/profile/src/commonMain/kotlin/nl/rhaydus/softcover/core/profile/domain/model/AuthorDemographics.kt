package nl.rhaydus.softcover.core.profile.domain.model

/**
 * Demographic breakdown of the user's distinct tagged authors across every finished book.
 * [bipocBreakdown] and [lgbtqBreakdown] are full Yes/No/Unknown breakdowns over every distinct
 * author, so the large unknown population is always visible rather than hidden behind a
 * share-of-tagged-authors fraction.
 */
data class AuthorDemographics(
    val genderSlices: List<GenderSlice> = emptyList(),
    val knownGenderCount: Int = 0,
    val unknownGenderCount: Int = 0,
    val bipocBreakdown: DemographicBreakdown = DemographicBreakdown(),
    val lgbtqBreakdown: DemographicBreakdown = DemographicBreakdown(),
)
