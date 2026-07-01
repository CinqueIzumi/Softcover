package nl.rhaydus.softcover.core.profile.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toSet
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

internal class ProfileRepositoryImpl(
    private val profileRemoteDataSource: ProfileRemoteDataSource,
    private val profileLocalDataSource: ProfileLocalDataSource,
) : ProfileRepository {
    override fun observeUserProfileData(): Flow<UserProfileData?> =
        profileLocalDataSource.observeUserProfileData()

    override suspend fun fetchUserProfileSnapshot(): UserProfileSnapshot {
        return profileRemoteDataSource.getUserProfileSnapshot()
    }

    override fun streamReadingDaysDescending(userId: Int): Flow<LocalDate> =
        profileRemoteDataSource.streamReadingDaysDescending(userId = userId)

    // The upstream stream is descending (newest first), so the recent window is exactly its
    // prefix: takeWhile collects it and cancels paging the instant a date crosses `since`.
    override suspend fun getActiveReadingDaysSince(
        userId: Int,
        since: LocalDate,
    ): Set<LocalDate> = streamReadingDaysDescending(userId = userId)
        .takeWhile { it >= since }
        .toSet()

    override suspend fun cacheUserProfileData(data: UserProfileData) {
        profileLocalDataSource.cacheUserProfileData(data = data)
    }

    override suspend fun markActiveReadingDate(date: LocalDate) {
        profileLocalDataSource.markActiveReadingDate(date = date)
    }

    override suspend fun clearProfileCache() {
        profileLocalDataSource.clear()
    }
}
