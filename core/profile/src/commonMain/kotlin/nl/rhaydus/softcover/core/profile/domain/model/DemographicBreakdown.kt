package nl.rhaydus.softcover.core.profile.domain.model

/**
 * A three-way Yes/No/Unknown breakdown of a boolean attribute (e.g. BIPOC, LGBTQ+) over ALL distinct
 * tagged authors - every author contributes to exactly one of [yesCount], [noCount], or
 * [unknownCount], so [total] is the full distinct-author count rather than only the subset with the
 * attribute known.
 */
data class DemographicBreakdown(
    val yesCount: Int = 0,
    val noCount: Int = 0,
    val unknownCount: Int = 0,
) {
    val total: Int get() = yesCount + noCount + unknownCount
}
