package nl.rhaydus.softcover.core.profile.domain.model

import nl.rhaydus.softcover.core.domain.model.Gender

/**
 * Demographic breakdown of the user's distinct tagged authors across every finished book.
 * [bipocBreakdown] and [lgbtqBreakdown] are full Yes/No/Unknown breakdowns over every distinct
 * author, so the large unknown population is always visible rather than hidden behind a
 * share-of-tagged-authors fraction. [excludingUntaggedAuthors] gives the opposite reading, for a
 * reader who wants the percentages among the tagged authors alone.
 */
data class AuthorDemographics(
    val genderSlices: List<GenderSlice> = emptyList(),
    val knownGenderCount: Int = 0,
    val unknownGenderCount: Int = 0,
    val bipocBreakdown: DemographicBreakdown = DemographicBreakdown(),
    val lgbtqBreakdown: DemographicBreakdown = DemographicBreakdown(),
) {
    /**
     * The same breakdown with every "we don't know" bucket dropped and the remaining shares
     * renormalised over the authors that are actually tagged.
     *
     * The exclusion is **per attribute**, not per author: an author with a known gender but no BIPOC
     * data still counts toward the gender slices and only drops out of the BIPOC breakdown. That
     * keeps the most data in each bar, at the cost of the three no longer sharing one denominator -
     * so a caller rendering these must state each bar's own total rather than one section-wide count.
     *
     * An attribute nobody is tagged for collapses to an empty/zero breakdown rather than to a
     * divide-by-zero: with [knownGenderCount] at 0 there are no gender slices left to renormalise.
     */
    fun excludingUntaggedAuthors(): AuthorDemographics = AuthorDemographics(
        genderSlices = genderSlices.excludingUnknownGender(knownGenderCount = knownGenderCount),
        knownGenderCount = knownGenderCount,
        unknownGenderCount = 0,
        bipocBreakdown = bipocBreakdown.copy(unknownCount = 0),
        lgbtqBreakdown = lgbtqBreakdown.copy(unknownCount = 0),
    )
}

private fun List<GenderSlice>.excludingUnknownGender(knownGenderCount: Int): List<GenderSlice> {
    if (knownGenderCount == 0) return emptyList()

    return filter { it.gender != Gender.Unknown }
        .map { slice -> slice.copy(fraction = slice.count.toDouble() / knownGenderCount) }
}
