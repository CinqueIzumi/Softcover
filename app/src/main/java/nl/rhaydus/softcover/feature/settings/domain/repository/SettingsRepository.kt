package nl.rhaydus.softcover.feature.settings.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.feature.settings.domain.model.SortDirection
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration

interface SettingsRepository {
    val dateStyle: Flow<DateStyle>

    val libraryGridLayout: Flow<LibraryGridLayout>

    val librarySortSettingsByTab: Flow<Map<String, LibrarySortSettings>>

    suspend fun setLibrarySortForTab(
        tabId: String,
        mode: LibrarySortMode,
        direction: SortDirection,
    )

    val dismissedPlanTodayByBook: Flow<Map<Int, String>>

    suspend fun setPlanTodayDismissed(
        bookId: Int,
        isoDate: String,
    )

    suspend fun setDateStyle(style: DateStyle)

    suspend fun setLibraryGridLayout(layout: LibraryGridLayout)

    suspend fun updateApiKey(key: String)

    fun getUserId(): Flow<Int>

    fun getThemeConfig(): Flow<ThemeConfiguration>

    suspend fun updateUserId(id: Int)

    suspend fun setBottomBarStyle(style: BottomBarStyle)

    suspend fun setDynamicColorEnabled(enabled: Boolean)

    suspend fun getUserIdFromBackend(): Int

    val enabledStatusCodes: Flow<Set<Int>>

    val enabledListIds: Flow<Set<Int>>

    val listDefaultsSeeded: Flow<Boolean>

    suspend fun seedEnabledListIds(ids: Set<Int>)

    suspend fun resetLibraryVisibilityPreferences()

    suspend fun setEnabledStatusCodes(codes: Set<Int>)

    suspend fun setEnabledListIds(ids: Set<Int>)

    val libraryTabOrder: Flow<List<String>>

    suspend fun setLibraryTabOrder(order: List<String>)
}