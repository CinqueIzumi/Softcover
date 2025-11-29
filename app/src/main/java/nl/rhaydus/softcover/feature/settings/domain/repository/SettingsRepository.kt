package nl.rhaydus.softcover.feature.settings.domain.repository

interface SettingsRepository {
    suspend fun updateApiKey(key: String)

    suspend fun getApiKey(): String
}