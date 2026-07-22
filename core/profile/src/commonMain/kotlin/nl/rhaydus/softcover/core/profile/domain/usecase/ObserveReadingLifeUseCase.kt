package nl.rhaydus.softcover.core.profile.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.profile.domain.model.RatingBand
import nl.rhaydus.softcover.core.profile.domain.model.RatingsDistribution
import nl.rhaydus.softcover.core.profile.domain.model.ReadingLife
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData

class ObserveReadingLifeUseCase(
    private val observeUserProfileDataUseCase: ObserveUserProfileDataUseCase,
) {
    // Every field - including genres now - comes straight off the cached, once-per-session network
    // aggregate; there is no live Room-derived data here (the local book cache only holds books
    // currently displayed in the Library, so it cannot back a complete stats view).
    operator fun invoke(): Flow<ReadingLife?> = observeUserProfileDataUseCase().map { profile ->
        profile?.toReadingLife()
    }

    private fun UserProfileData.toReadingLife(): ReadingLife = ReadingLife(
        booksByYear = booksByYear,
        pagesByYear = pagesByYear,
        pagesByMonth = pagesByMonth,
        genres = genres,
        ratings = ratings.classified(),
        recentlyLoved = recentlyLoved,
        trackedYears = trackedYears,
        authorDemographics = authorDemographics,
    )

    // band is never trusted from the cache (it is not persisted there in the first place); it is
    // always recomputed from the buckets/average/total that ARE cached.
    private fun RatingsDistribution.classified(): RatingsDistribution = copy(
        band = RatingBand.classify(
            halfStarBuckets = halfStarBuckets,
            average = average,
            totalRatings = totalRatings,
        ),
    )
}
