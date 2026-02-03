package nl.rhaydus.softcover.feature.settings.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import nl.rhaydus.softcover.feature.settings.domain.model.UserInformation

class SettingsRepositoryImpl(
    private val settingsLocalDataSource: SettingsLocalDataSource,
    private val settingsRemoteDataSource: SettingsRemoteDataSource,
) : SettingsRepository {
    override suspend fun updateApiKey(key: String) {
        settingsLocalDataSource.updateApiKey(key = key)
    }

    override suspend fun getApiKey(): String {
        return settingsLocalDataSource.getApiKey()
    }

    override fun getUserId(): Flow<Int> {
        return settingsLocalDataSource.getUserId()
    }

    override suspend fun updateUserId(id: Int) {
        settingsLocalDataSource.updateUserId(id = id)
    }

    override suspend fun getUserInfoFromBackend(): UserInformation {
        return settingsRemoteDataSource.getUserInformation()
    }
}