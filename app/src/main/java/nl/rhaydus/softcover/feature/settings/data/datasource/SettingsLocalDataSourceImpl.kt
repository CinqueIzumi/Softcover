package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore
import nl.rhaydus.softcover.feature.settings.data.model.AppSettingsEntity
import nl.rhaydus.softcover.feature.settings.data.model.toModel
import nl.rhaydus.softcover.feature.settings.domain.model.BottomBarStyle
import nl.rhaydus.softcover.feature.settings.domain.model.DateStyle
import nl.rhaydus.softcover.feature.settings.domain.model.ThemeConfiguration

class SettingsLocalDataSourceImpl(
    private val appSettingsDataStore: AppSettingsDataStore,
    private val apiKeyLocalDataSource: ApiKeyLocalDataSource,
) : SettingsLocalDataSource {
    override val dateStyle: Flow<DateStyle> = appSettingsDataStore.store.data
        .map { it.dateStyle }
        .distinctUntilChanged()

    override suspend fun setDateStyle(style: DateStyle) {
        appSettingsDataStore.store.updateData {
            it.copy(dateStyle = style)
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
}