package nl.rhaydus.softcover.feature.profile.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.profile.data.datasource.ProfileLocalDataSource
import nl.rhaydus.softcover.feature.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.feature.profile.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val profileRemoteDataSource: ProfileRemoteDataSource,
    private val profileLocalDataSource: ProfileLocalDataSource,
) : ProfileRepository {
    override fun observeUserProfileData(): Flow<UserProfileData?> =
        profileLocalDataSource.observeUserProfileData()

    override suspend fun fetchUserProfileSnapshot(userId: Int): UserProfileSnapshot {
        return profileRemoteDataSource.getUserProfileSnapshot(userId = userId)
    }

    override suspend fun cacheUserProfileData(data: UserProfileData) {
        profileLocalDataSource.cacheUserProfileData(data = data)
    }

    override suspend fun clearProfileCache() {
        profileLocalDataSource.clear()
    }
}
