package nl.rhaydus.softcover.core.profile.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.core.profile.domain.ProfileRefreshGate
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

internal class ProfileRepositoryImpl(
    private val profileRemoteDataSource: ProfileRemoteDataSource,
    private val profileLocalDataSource: ProfileLocalDataSource,
    private val activityRefreshGate: ProfileRefreshGate,
    private val statsRefreshGate: ProfileRefreshGate,
) : ProfileRepository {
    override fun observeUserProfileData(): Flow<UserProfileData?> =
        profileLocalDataSource.observeUserProfileData()

    override suspend fun fetchUserProfileSnapshot(): UserProfileSnapshot {
        return profileRemoteDataSource.getUserProfileSnapshot()
    }

    override fun streamReadingDaysDescending(userId: Int): Flow<LocalDate> =
        profileRemoteDataSource.streamReadingDaysDescending(userId = userId)

    override suspend fun cacheUserProfileActivity(
        readingStreak: Int,
        recentReadingDays: Set<LocalDate>,
    ) {
        profileLocalDataSource.cacheUserProfileActivity(
            readingStreak = readingStreak,
            recentReadingDays = recentReadingDays,
        )
    }

    override suspend fun cacheUserProfileStats(snapshot: UserProfileSnapshot) {
        profileLocalDataSource.cacheUserProfileStats(snapshot = snapshot)
    }

    override suspend fun markActiveReadingDate(date: LocalDate) {
        profileLocalDataSource.markActiveReadingDate(date = date)
    }

    // Resets both refresh gates: a partial session (only one half ever succeeded) must not leave
    // the other half permanently skipped for the next login, and a full logout needs both halves
    // to refetch from zero regardless of which had already run.
    override suspend fun clearProfileCache() {
        profileLocalDataSource.clear()
        activityRefreshGate.reset()
        statsRefreshGate.reset()
    }
}
