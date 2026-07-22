package nl.rhaydus.softcover.core.profile.domain.model

private const val HALF_STAR_BUCKET_COUNT = 9

data class RatingsDistribution(
    val average: Double = 0.0,
    val totalRatings: Int = 0,
    val halfStarBuckets: List<Int> = List(HALF_STAR_BUCKET_COUNT) { 0 },
    val band: RatingBand = RatingBand.NEW_READER,
)
