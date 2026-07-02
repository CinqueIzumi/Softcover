package nl.rhaydus.softcover.core.preferences.domain.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DesktopWindowState
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.UiScale

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

    /**
     * Resets **all** app settings to their defaults (theme, layout, sort, library visibility, userId,
     * etc.) — the preference half of the full local-data wipe used by logout / account-switch. The
     * API key lives in secure storage and is cleared separately via [updateApiKey].
     */
    suspend fun resetAllSettings()

    suspend fun setEnabledStatusCodes(codes: Set<Int>)

    suspend fun setEnabledListIds(ids: Set<Int>)

    val libraryTabOrder: Flow<List<String>>

    suspend fun setLibraryTabOrder(order: List<String>)

    val desktopWindowState: Flow<DesktopWindowState>

    suspend fun setDesktopWindowState(state: DesktopWindowState)

    val readingStreakEnabled: Flow<Boolean>

    suspend fun setReadingStreakEnabled(enabled: Boolean)

    val uiScale: Flow<UiScale>

    suspend fun setUiScale(scale: UiScale)

    val lastUsedProgressUnit: Flow<ProgressUnit>

    suspend fun setLastUsedProgressUnit(unit: ProgressUnit)
}
