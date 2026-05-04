package nl.rhaydus.softcover.feature.profile.data.repository

import nl.rhaydus.softcover.feature.profile.data.datasource.ProfileRemoteDataSource
import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileSnapshot
import nl.rhaydus.softcover.feature.profile.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val profileRemoteDataSource: ProfileRemoteDataSource,
) : ProfileRepository {
    override suspend fun getUserProfileSnapshot(userId: Int): UserProfileSnapshot {
        return profileRemoteDataSource.getUserProfileSnapshot(userId = userId)
    }
}
