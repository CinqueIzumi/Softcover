package nl.rhaydus.softcover.feature.settings.data.datasource

interface SettingsLocalDataSource {
    suspend fun updateApiKey(key: String)

    suspend fun getApiKey(): String

    suspend fun getUserId(): Int

    suspend fun updateUserId(id: Int)
}