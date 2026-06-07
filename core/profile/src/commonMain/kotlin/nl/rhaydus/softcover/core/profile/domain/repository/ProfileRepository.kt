package nl.rhaydus.softcover.core.profile.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

interface ProfileRepository {
    fun observeUserProfileData(): Flow<UserProfileData?>

    suspend fun fetchUserProfileSnapshot(userId: Int): UserProfileSnapshot

    suspend fun cacheUserProfileData(data: UserProfileData)

    suspend fun clearProfileCache()
}
