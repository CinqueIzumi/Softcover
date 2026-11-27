package nl.rhaydus.softcover.core.profile.domain.model

data class DemographicBreakdown(
    val yesCount: Int = 0,
    val noCount: Int = 0,
    val unknownCount: Int = 0,
) {
    val total: Int get() = yesCount + noCount + unknownCount
}
