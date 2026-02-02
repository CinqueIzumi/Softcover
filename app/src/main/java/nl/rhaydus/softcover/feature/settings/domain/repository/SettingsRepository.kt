package nl.rhaydus.softcover.feature.settings.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.UserInformation

interface SettingsRepository {
    suspend fun updateApiKey(key: String)

    suspend fun getApiKey(): String

    fun getUserId(): Flow<Int>

    suspend fun updateUserId(id: Int)

    suspend fun getUserInfoFromBackend(): UserInformation
}