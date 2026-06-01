package nl.rhaydus.softcover.feature.settings.data.repository

import kotlinx.coroutines.flow.Flow
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsLocalDataSource
import nl.rhaydus.softcover.feature.settings.data.datasource.SettingsRemoteDataSource
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val settingsLocalDataSource: SettingsLocalDataSource,
    private val settingsRemoteDataSource: SettingsRemoteDataSource,
) : SettingsRepository {
    override val dateStyle: Flow<DateStyle> = settingsLocalDataSource.dateStyle

    override val libraryGridLayout: Flow<LibraryGridLayout> = settingsLocalDataSource.libraryGridLayout

    override suspend fun setDateStyle(style: DateStyle) {
        settingsLocalDataSource.setDateStyle(style = style)
    }

    override suspend fun setLibraryGridLayout(layout: LibraryGridLayout) {
        settingsLocalDataSource.setLibraryGridLayout(layout = layout)
    }

    override val librarySortSettingsByTab: Flow<Map<String, LibrarySortSettings>> =
        settingsLocalDataSource.librarySortSettingsByTab

    override suspend fun setLibrarySortForTab(
        tabId: String,
        mode: LibrarySortMode,
        direction: SortDirection,
    ) {
        settingsLocalDataSource.setLibrarySortForTab(
            tabId = tabId,
            mode = mode,
            direction = direction,
        )
    }

    override val dismissedPlanTodayByBook: Flow<Map<Int, String>> =
        settingsLocalDataSource.dismissedPlanTodayByBook

    override suspend fun setPlanTodayDismissed(
        bookId: Int,
        isoDate: String,
    ) {
        settingsLocalDataSource.setPlanTodayDismissed(
            bookId = bookId,
            isoDate = isoDate,
        )
    }

    override suspend fun updateApiKey(key: String) {
        settingsLocalDataSource.updateApiKey(key = key)
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

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        settingsLocalDataSource.setDynamicColorEnabled(enabled = enabled)
    }

    override suspend fun getUserIdFromBackend(): Int {
        return settingsRemoteDataSource.getUserIdFromBackend()
    }

    override val enabledStatusCodes: Flow<Set<Int>> = settingsLocalDataSource.enabledStatusCodes

    override val enabledListIds: Flow<Set<Int>> = settingsLocalDataSource.enabledListIds

    override val listDefaultsSeeded: Flow<Boolean> = settingsLocalDataSource.listDefaultsSeeded

    override suspend fun seedEnabledListIds(ids: Set<Int>) {
        settingsLocalDataSource.seedEnabledListIds(ids = ids)
    }

    override suspend fun resetLibraryVisibilityPreferences() {
        settingsLocalDataSource.resetLibraryVisibilityPreferences()
    }

    override suspend fun setEnabledStatusCodes(codes: Set<Int>) {
        settingsLocalDataSource.setEnabledStatusCodes(codes = codes)
    }

    override suspend fun setEnabledListIds(ids: Set<Int>) {
        settingsLocalDataSource.setEnabledListIds(ids = ids)
    }

    override val libraryTabOrder: Flow<List<String>> = settingsLocalDataSource.libraryTabOrder

    override suspend fun setLibraryTabOrder(order: List<String>) {
        settingsLocalDataSource.setLibraryTabOrder(order = order)
    }
}