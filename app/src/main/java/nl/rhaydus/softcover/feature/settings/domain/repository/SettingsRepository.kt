package nl.rhaydus.softcover.feature.settings.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.settings.domain.model.UserProfileData

interface SettingsRepository {
    val dateStyle: Flow<DateStyle>

    val libraryGridLayout: Flow<LibraryGridLayout>

    suspend fun setDateStyle(style: DateStyle)

    suspend fun setLibraryGridLayout(layout: LibraryGridLayout)

    suspend fun updateApiKey(key: String)

    fun getUserId(): Flow<Int>

    fun getThemeConfig(): Flow<ThemeConfiguration>

    suspend fun updateUserId(id: Int)

    suspend fun setBottomBarStyle(style: BottomBarStyle)

    suspend fun getUserIdFromBackend(): Int

    suspend fun getUserProfileData(): UserProfileData

    val enabledStatusCodes: Flow<Set<Int>>

    val enabledListIds: Flow<Set<Int>>

    val listDefaultsSeeded: Flow<Boolean>

    suspend fun seedEnabledListIds(ids: Set<Int>)

    suspend fun resetLibraryVisibilityPreferences()

    suspend fun setEnabledStatusCodes(codes: Set<Int>)

    suspend fun setEnabledListIds(ids: Set<Int>)
}