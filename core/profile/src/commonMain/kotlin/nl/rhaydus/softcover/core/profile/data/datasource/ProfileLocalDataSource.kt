package nl.rhaydus.softcover.core.profile.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.profile.data.datastore.ProfileCacheDataStore
import nl.rhaydus.softcover.core.profile.data.model.toEntity
import nl.rhaydus.softcover.core.profile.data.model.toModel
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

interface ProfileLocalDataSource {
    fun observeUserProfileData(): Flow<UserProfileData?>

    suspend fun cacheUserProfileActivity(
        readingStreak: Int,
        recentReadingDays: Set<LocalDate>,
    )

    suspend fun cacheUserProfileStats(snapshot: UserProfileSnapshot)

    suspend fun markActiveReadingDate(date: LocalDate)

    suspend fun clear()
}

// Neutral stand-in for whichever half of UserProfileData hasn't been fetched yet, so the other
// half's writer always has something to merge into instead of special-casing a null cache.
private val emptyUserProfileData = UserProfileData(
    profileImageUrl = "",
    name = "",
    username = "",
    bio = "",
    booksRead = 0,
    totalPagesRead = 0,
    averageRating = 0.0,
    readingStreak = 0,
)

internal class ProfileLocalDataSourceImpl(
    private val profileCacheDataStore: ProfileCacheDataStore,
) : ProfileLocalDataSource {
    override fun observeUserProfileData(): Flow<UserProfileData?> =
        profileCacheDataStore.store.data
            .map { it.profile?.toModel() }
            .distinctUntilChanged()

    // Merges into the current cache (or the neutral stand-in on a first-ever refresh) so a stats
    // refresh that hasn't run yet, or already ran, is left untouched.
    override suspend fun cacheUserProfileActivity(
        readingStreak: Int,
        recentReadingDays: Set<LocalDate>,
    ) {
        profileCacheDataStore.store.updateData { cache ->
            val current = cache.profile?.toModel() ?: emptyUserProfileData

            cache.copy(
                profile = current.copy(
                    readingStreak = readingStreak,
                    recentReadingDays = recentReadingDays,
                ).toEntity(),
            )
        }
    }

    // Merges into the current cache, carrying the existing readingStreak/recentReadingDays forward
    // untouched so a stats refresh never blanks activity data an earlier refresh already wrote.
    override suspend fun cacheUserProfileStats(snapshot: UserProfileSnapshot) {
        profileCacheDataStore.store.updateData { cache ->
            val current = cache.profile?.toModel() ?: emptyUserProfileData

            cache.copy(
                profile = current.copy(
                    profileImageUrl = snapshot.profileImageUrl,
                    name = snapshot.name,
                    username = snapshot.username,
                    bio = snapshot.bio,
                    booksRead = snapshot.booksRead,
                    totalPagesRead = snapshot.totalPagesRead,
                    averageRating = snapshot.averageRating,
                    booksByYear = snapshot.booksByYear,
                    pagesByYear = snapshot.pagesByYear,
                    pagesByMonth = snapshot.pagesByMonth,
                    genres = snapshot.genres,
                    ratings = snapshot.ratings,
                    recentlyLoved = snapshot.recentlyLoved,
                    trackedYears = snapshot.trackedYears,
                    authorDemographics = snapshot.authorDemographics,
                ).toEntity(),
            )
        }
    }

    override suspend fun markActiveReadingDate(date: LocalDate) {
        val isoDate = date.toString()

        profileCacheDataStore.store.updateData { cache ->
            // No cached profile yet → nothing to merge into; the next server refresh will carry it.
            val profile = cache.profile ?: return@updateData cache

            if (isoDate in profile.recentReadingDays) return@updateData cache

            cache.copy(
                profile = profile.copy(
                    recentReadingDays = (profile.recentReadingDays + isoDate).sorted(),
                ),
            )
        }
    }

    override suspend fun clear() {
        profileCacheDataStore.store.updateData {
            it.copy(profile = null)
        }
    }
}
