package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.feature.settings.data.model.AppSettingsEntity
import nl.rhaydus.softcover.feature.settings.data.model.toModel
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.LibraryGridLayout
import nl.rhaydus.softcover.feature.settings.domain.model.LibrarySortMode
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration

class SettingsLocalDataSourceImpl(
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

    override val librarySortModeByTab: Flow<Map<String, LibrarySortMode>> =
        appSettingsDataStore.store.data
            .map { it.librarySortModeByTab }
            .distinctUntilChanged()

    override suspend fun setLibrarySortModeForTab(
        tabId: String,
        mode: LibrarySortMode,
    ) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(librarySortModeByTab = entity.librarySortModeByTab + (tabId to mode))
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

    override val libraryTabOrder: Flow<List<String>> = appSettingsDataStore.store.data
        .map { it.libraryTabOrder }
        .distinctUntilChanged()

    override suspend fun setLibraryTabOrder(order: List<String>) {
        appSettingsDataStore.store.updateData { entity ->
            entity.copy(libraryTabOrder = order)
        }
    }
}