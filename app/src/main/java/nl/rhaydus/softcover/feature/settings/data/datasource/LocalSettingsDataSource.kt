package nl.rhaydus.softcover.feature.settings.data.datasource

interface LocalSettingsDataSource {
    suspend fun updateApiKey(key: String)

    suspend fun getApiKey(): String
}