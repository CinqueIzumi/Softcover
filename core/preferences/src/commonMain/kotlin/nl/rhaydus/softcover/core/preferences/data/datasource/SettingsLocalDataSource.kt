package nl.rhaydus.softcover.core.preferences.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.domain.model.BottomBarStyle
import nl.rhaydus.softcover.core.domain.model.ColorPalette
import nl.rhaydus.softcover.core.domain.model.DateStyle
import nl.rhaydus.softcover.core.domain.model.DesktopWindowState
import nl.rhaydus.softcover.core.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.core.domain.model.LibrarySortMode
import nl.rhaydus.softcover.core.domain.model.LibrarySortSettings
import nl.rhaydus.softcover.core.domain.model.ProgressUnit
import nl.rhaydus.softcover.core.domain.model.SortDirection
import nl.rhaydus.softcover.core.domain.model.ThemeConfiguration
import nl.rhaydus.softcover.core.domain.model.ThemeMode
import nl.rhaydus.softcover.core.domain.model.UiScale
import nl.rhaydus.softcover.core.preferences.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity
import nl.rhaydus.softcover.core.preferences.data.model.toEntity
import nl.rhaydus.softcover.core.preferences.data.model.toModel

interface SettingsLocalDataSource {
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

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setColorPalette(palette: ColorPalette)

    suspend fun setDynamicColorEnabled(enabled: Boolean)

    val enabledStatusCodes: Flow<Set<Int>>

    val enabledListIds: Flow<Set<Int>>

    val listDefaultsSeeded: Flow<Boolean>

    suspend fun seedEnabledListIds(ids: Set<Int>)

    suspend fun resetLibraryVisibilityPreferences()

    suspend fun resetAllSettings()

    suspend fun setEnabledStatusCodes(codes: Set<Int>)

    suspend fun setEnabledListIds(ids: Set<Int>)

    val libraryTabOrder: Flow<List<String>>

    suspend fun setLibraryTabOrder(order: List<String>)

    val desktopWindowState: Flow<DesktopWindowState>

    suspend fun setDesktopWindowState(state: DesktopWindowState)

    val readingStreakEnabled: Flow<Boolean>

    suspend fun setReadingStreakEnabled(enabled: Boolean)

    val shelfSwipeEnabled: Flow<Boolean>

    suspend fun setShelfSwipeEnabled(enabled: Boolean)

    val hideUntaggedAuthors: Flow<Boolean>

    suspend fun setHideUntaggedAuthors(enabled: Boolean)

    val uiScale: Flow<UiScale>

    suspend fun setUiScale(scale: UiScale)

    val lastUsedProgressUnit: Flow<ProgressUnit>

    suspend fun setLastUsedProgressUnit(unit: ProgressUnit)

    val becauseYouReadGenre: Flow<String?>

    suspend fun setBecauseYouReadGenre(genre: String?)
}

internal class SettingsLocalDataSourceImpl(
    private val appSettingsDataStore: AppSettingsDataStore,
    private val apiKeyLocalDataSource: ApiKeyLocalDataSource,
) : SettingsLocalDataSource {
    override val dateStyle: Flow<DateStyle> = appSettingsDataStore.store.data
        .map { it.dateStyle }
        .distinctUntilChanged()

    override val libraryGridLayout: Flow<LibraryGridLayout> = appSettingsDataStore.store.data
        .map { it.libraryGridLayout }
        .distinctUntilChanged()

    override suspend fun setDateStyle(style: DateStyle) {
        appSettingsDataStore.store.updateData {
            it.copy(dateStyle = style)
        }
    }

    override suspend fun setLibraryGridLayout(layout: LibraryGridLayout) {
        appSettingsDataStore.store.updateData {
            it.copy(libraryGridLayout = layout)
        }
    }

    // Reading mode + direction from the same upstream entity emission collapses to a single flow
    // emission per write, which is what lets the library screen avoid a transient (newMode, oldDir)
    // intermediate state during a sort change.
    override val librarySortSettingsByTab: Flow<Map<String, LibrarySortSettings>> =
        appSettingsDataStore.store.data
            .map { entity ->
                val tabIds = entity.librarySortModeByTab.keys + entity.librarySortDirectionByTab.keys

                tabIds.associateWith { tabId ->
                    val mode = entity.librarySortModeByTab[tabId] ?: LibrarySortMode.Default

                    LibrarySortSettings(
                        mode = mode,
                        direction = entity.librarySortDirectionByTab[tabId] ?: mode.defaultDirection,
                    )
                }
            }
            .distinctUntilChanged()

    override suspend fun setLibrarySortForTab(
        tabId: String,
        mode: LibrarySortMode,
        direction: SortDirection,
    ) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(
                librarySortModeByTab = entity.librarySortModeByTab + (tabId to mode),
                librarySortDirectionByTab = entity.librarySortDirectionByTab + (tabId to direction),
            )
        }
    }

    override val dismissedPlanTodayByBook: Flow<Map<Int, String>> =
        appSettingsDataStore.store.data
            .map { it.dismissedPlanTodayByBook }
            .distinctUntilChanged()

    override suspend fun setPlanTodayDismissed(
        bookId: Int,
        isoDate: String,
    ) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(dismissedPlanTodayByBook = entity.dismissedPlanTodayByBook + (bookId to isoDate))
        }
    }

    override suspend fun updateApiKey(key: String) {
        apiKeyLocalDataSource.updateApiKey(key = key)
    }

    override fun getUserId(): Flow<Int> {
        return appSettingsDataStore.store.data.map { it.userId }.distinctUntilChanged()
    }

    override fun getThemeConfig(): Flow<ThemeConfiguration> {
        return appSettingsDataStore.store.data
            .map { it.themeConfig.toModel() }
            .distinctUntilChanged()
    }

    override suspend fun updateUserId(id: Int) {
        appSettingsDataStore.store.updateData {
            it.copy(userId = id)
        }
    }

    override suspend fun setBottomBarStyle(style: BottomBarStyle) {
        appSettingsDataStore.store.updateData { appSettingsEntity: AppSettingsEntity ->
            appSettingsEntity.copy(themeConfig = appSettingsEntity.themeConfig.copy(bottomBarStyle = style))
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        appSettingsDataStore.store.updateData { appSettingsEntity: AppSettingsEntity ->
            appSettingsEntity.copy(themeConfig = appSettingsEntity.themeConfig.copy(themeMode = mode))
        }
    }

    override suspend fun setColorPalette(palette: ColorPalette) {
        appSettingsDataStore.store.updateData { appSettingsEntity: AppSettingsEntity ->
            appSettingsEntity.copy(themeConfig = appSettingsEntity.themeConfig.copy(colorPalette = palette))
        }
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean) {
        appSettingsDataStore.store.updateData { appSettingsEntity: AppSettingsEntity ->
            appSettingsEntity.copy(themeConfig = appSettingsEntity.themeConfig.copy(useDynamicColor = enabled))
        }
    }

    override val enabledStatusCodes: Flow<Set<Int>> = appSettingsDataStore.store.data
        .map { it.enabledStatusCodes }
        .distinctUntilChanged()

    override val enabledListIds: Flow<Set<Int>> = appSettingsDataStore.store.data
        .map { it.enabledListIds }
        .distinctUntilChanged()

    override val listDefaultsSeeded: Flow<Boolean> = appSettingsDataStore.store.data
        .map { it.listDefaultsSeeded }
        .distinctUntilChanged()

    override suspend fun seedEnabledListIds(ids: Set<Int>) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(
                enabledListIds = entity.enabledListIds + ids,
                listDefaultsSeeded = true,
            )
        }
    }

    override suspend fun setEnabledStatusCodes(codes: Set<Int>) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(enabledStatusCodes = codes)
        }
    }

    override suspend fun setEnabledListIds(ids: Set<Int>) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(enabledListIds = ids)
        }
    }

    override suspend fun resetLibraryVisibilityPreferences() {
        val defaults = AppSettingsEntity()

        appSettingsDataStore.store.updateData { entity ->
            entity.copy(
                enabledStatusCodes = defaults.enabledStatusCodes,
                enabledListIds = defaults.enabledListIds,
                listDefaultsSeeded = defaults.listDefaultsSeeded,
                libraryTabOrder = defaults.libraryTabOrder,
            )
        }
    }

    override suspend fun resetAllSettings() {
        appSettingsDataStore.store.updateData { AppSettingsEntity() }
    }

    override val libraryTabOrder: Flow<List<String>> = appSettingsDataStore.store.data
        .map { it.libraryTabOrder }
        .distinctUntilChanged()

    override suspend fun setLibraryTabOrder(order: List<String>) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(libraryTabOrder = order)
        }
    }

    override val desktopWindowState: Flow<DesktopWindowState> = appSettingsDataStore.store.data
        .map { it.desktopWindowState.toModel() }
        .distinctUntilChanged()

    override suspend fun setDesktopWindowState(state: DesktopWindowState) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(desktopWindowState = state.toEntity())
        }
    }

    override val readingStreakEnabled: Flow<Boolean> = appSettingsDataStore.store.data
        .map { it.readingStreakEnabled }
        .distinctUntilChanged()

    override suspend fun setReadingStreakEnabled(enabled: Boolean) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(readingStreakEnabled = enabled)
        }
    }

    override val shelfSwipeEnabled: Flow<Boolean> = appSettingsDataStore.store.data
        .map { it.shelfSwipeEnabled }
        .distinctUntilChanged()

    override suspend fun setShelfSwipeEnabled(enabled: Boolean) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(shelfSwipeEnabled = enabled)
        }
    }

    override val hideUntaggedAuthors: Flow<Boolean> = appSettingsDataStore.store.data
        .map { it.hideUntaggedAuthors }
        .distinctUntilChanged()

    override suspend fun setHideUntaggedAuthors(enabled: Boolean) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(hideUntaggedAuthors = enabled)
        }
    }

    override val uiScale: Flow<UiScale> = appSettingsDataStore.store.data
        .map { it.uiScale }
        .distinctUntilChanged()

    override suspend fun setUiScale(scale: UiScale) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(uiScale = scale)
        }
    }

    override val lastUsedProgressUnit: Flow<ProgressUnit> = appSettingsDataStore.store.data
        .map { it.lastUsedProgressUnit }
        .distinctUntilChanged()

    override suspend fun setLastUsedProgressUnit(unit: ProgressUnit) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(lastUsedProgressUnit = unit)
        }
    }

    override val becauseYouReadGenre: Flow<String?> = appSettingsDataStore.store.data
        .map { it.becauseYouReadGenre }
        .distinctUntilChanged()

    override suspend fun setBecauseYouReadGenre(genre: String?) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(becauseYouReadGenre = genre)
        }
    }
}
