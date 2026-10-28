package nl.rhaydus.softcover.core.profile.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileLocalDataSource
import nl.rhaydus.softcover.core.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.core.profile.domain.ProfileRefreshGate
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.core.profile.domain.repository.ProfileRepository

internal class ProfileRepositoryImpl(
    private val profileRemoteDataSource: ProfileRemoteDataSource,
    private val profileLocalDataSource: ProfileLocalDataSource,
    private val statsRefreshGate: ProfileRefreshGate,
) : ProfileRepository {
    override fun observeUserProfileData(): Flow<UserProfileData?> =
        profileLocalDataSource.observeUserProfileData()

    override suspend fun fetchUserProfileSnapshot(): UserProfileSnapshot {
        return profileRemoteDataSource.getUserProfileSnapshot()
    }

    override suspend fun cacheUserProfileIdentity(snapshot: UserProfileSnapshot) {
        profileLocalDataSource.cacheUserProfileIdentity(snapshot = snapshot)
    }

    // A full logout needs the stats refresh to run again from zero on the next login.
    override suspend fun clearProfileCache() {
        profileLocalDataSource.clear()
        statsRefreshGate.reset()
    }
}
