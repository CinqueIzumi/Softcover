package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.flow.Flow

interface SettingsLocalDataSource {
    suspend fun updateApiKey(key: String)

    suspend fun getApiKey(): String

    fun getUserId(): Flow<Int>

    suspend fun updateUserId(id: Int)
}