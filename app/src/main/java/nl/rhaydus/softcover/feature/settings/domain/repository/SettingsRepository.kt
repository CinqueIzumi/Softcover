package nl.rhaydus.softcover.feature.settings.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.UserProfileData

interface SettingsRepository {
    suspend fun updateApiKey(key: String)

    suspend fun getApiKey(): String

    fun getUserId(): Flow<Int>

    suspend fun updateUserId(id: Int)

    suspend fun getUserIdFromBackend(): Int

    suspend fun getUserProfileData(): UserProfileData
}