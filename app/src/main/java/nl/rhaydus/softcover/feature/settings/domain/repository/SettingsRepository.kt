package nl.rhaydus.softcover.feature.settings.domain.repository

interface SettingsRepository {
    suspend fun updateApiKey(key: String)

    suspend fun getApiKey(): String

    suspend fun getUserId(): Int

    suspend fun updateUserId(id: Int)

    suspend fun getUserIdFromBackend(): Int
}