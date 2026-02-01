package nl.rhaydus.softcover.feature.settings.data.datasource

interface SettingsRemoteDataSource {
    suspend fun getUserId(): Int
} 