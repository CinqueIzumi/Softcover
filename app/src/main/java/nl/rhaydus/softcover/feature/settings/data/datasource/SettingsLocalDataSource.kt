package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration

interface SettingsLocalDataSource {
    val dateStyle: Flow<DateStyle>

    suspend fun setDateStyle(style: DateStyle)

    suspend fun updateApiKey(key: String)

    fun getUserId(): Flow<Int>

    fun getThemeConfig(): Flow<ThemeConfiguration>

    suspend fun updateUserId(id: Int)

    suspend fun setBottomBarStyle(style: BottomBarStyle)
}