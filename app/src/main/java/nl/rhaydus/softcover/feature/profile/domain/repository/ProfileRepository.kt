package nl.rhaydus.softcover.feature.profile.domain.repository

import nl.rhaydus.softcover.feature.profile.domain.model.UserProfileSnapshot

interface ProfileRepository {
    suspend fun getUserProfileSnapshot(userId: Int): UserProfileSnapshot
}
