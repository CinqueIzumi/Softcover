package nl.rhaydus.softcover.feature.settings.data.repository

import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Provider

class SettingsRepositoryImpl @Inject constructor(
    private val settingsLocalDataSource: SettingsLocalDataSource,
    private val settingsRemoteDataSource: Provider<SettingsRemoteDataSource>,
) : SettingsRepository {
    override suspend fun updateApiKey(key: String) {
        settingsLocalDataSource.updateApiKey(key = key)
    }

    override suspend fun getApiKey(): String {
        return settingsLocalDataSource.getApiKey()
    }

    override suspend fun getUserId(): Int {
        return settingsLocalDataSource.getUserId()
    }

    override suspend fun updateUserId(id: Int) {
        settingsLocalDataSource.updateUserId(id = id)
    }

    override suspend fun getUserIdFromBackend(): Int {
        return settingsRemoteDataSource.get().getUserId()
    }
}