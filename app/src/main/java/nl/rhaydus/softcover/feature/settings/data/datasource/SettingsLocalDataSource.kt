package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration

interface SettingsLocalDataSource {
    suspend fun updateApiKey(key: String)

    suspend fun getApiKey(): String

    fun getUserId(): Flow<Int>

    fun getThemeConfig(): Flow<ThemeConfiguration>

    suspend fun updateUserId(id: Int)

    suspend fun setBottomBarStyle(style: BottomBarStyle)
}