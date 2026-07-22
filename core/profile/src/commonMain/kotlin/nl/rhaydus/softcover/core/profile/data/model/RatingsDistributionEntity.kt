package nl.rhaydus.softcover.core.profile.data.model

import kotlinx.serialization.Serializable
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution

// band is deliberately not cached: it is a pure function of the other three fields, always
// recomputed by ObserveReadingLifeUseCase via RatingBand.classify, so persisting it would just be a
// stale duplicate.
@Serializable
internal data class RatingsDistributionEntity(
    val average: Double = 0.0,
    val totalRatings: Int = 0,
    val halfStarBuckets: List<Int> = emptyList(),
)

internal fun RatingsDistributionEntity.toModel(): RatingsDistribution = RatingsDistribution(
    average = average,
    totalRatings = totalRatings,
    halfStarBuckets = halfStarBuckets.ifEmpty { RatingsDistribution().halfStarBuckets },
)

internal fun RatingsDistribution.toEntity(): RatingsDistributionEntity = RatingsDistributionEntity(
    average = average,
    totalRatings = totalRatings,
    halfStarBuckets = halfStarBuckets,
)
