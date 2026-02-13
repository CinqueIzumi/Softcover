package nl.rhaydus.softcover.feature.settings.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.settings.domain.model.UserProfileData
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

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

    override fun getThemeConfig(): Flow<ThemeConfiguration> {
        return settingsLocalDataSource.getThemeConfig()
    }

    override suspend fun updateUserId(id: Int) {
        settingsLocalDataSource.updateUserId(id = id)
    }

    override suspend fun setBottomBarStyle(style: BottomBarStyle) {
        settingsLocalDataSource.setBottomBarStyle(style = style)
    }

    override suspend fun getUserIdFromBackend(): Int {
        return settingsRemoteDataSource.getUserIdFromBackend()
    }

    override suspend fun getUserProfileData(): UserProfileData {
        return settingsRemoteDataSource.getUserProfileData()
    }
}