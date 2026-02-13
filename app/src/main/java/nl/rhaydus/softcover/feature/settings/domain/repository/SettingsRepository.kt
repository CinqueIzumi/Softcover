package nl.rhaydus.softcover.feature.settings.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.settings.domain.model.UserProfileData

interface SettingsRepository {

    suspend fun updateApiKey(key: String)

    suspend fun getApiKey(): String

    fun getUserId(): Flow<Int>

    fun getThemeConfig(): Flow<ThemeConfiguration>

    suspend fun updateUserId(id: Int)

    suspend fun setBottomBarStyle(style: BottomBarStyle)

    suspend fun getUserIdFromBackend(): Int

    suspend fun getUserProfileData(): UserProfileData
}