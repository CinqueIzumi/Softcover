package nl.rhaydus.softcover.core.profile.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileSnapshot

interface ProfileRepository {
    fun observeUserProfileData(): Flow<UserProfileData?>

    suspend fun fetchUserProfileSnapshot(): UserProfileSnapshot

    suspend fun cacheUserProfileIdentity(snapshot: UserProfileSnapshot)

    suspend fun clearProfileCache()
}
