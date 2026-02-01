package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore

class SettingsLocalDataSourceImpl(
    private val appSettingsDataStore: AppSettingsDataStore,
) : SettingsLocalDataSource {
    override suspend fun updateApiKey(key: String) {
        appSettingsDataStore.store.updateData {
            it.copy(apiKey = key)
        }
    }

    override suspend fun getApiKey(): String {
        return appSettingsDataStore.store.data.first().apiKey
    }

    override fun getUserId(): Flow<Int> {
        return appSettingsDataStore.store.data.map { it.userId }.distinctUntilChanged()
    }

    override suspend fun updateUserId(id: Int) {
        appSettingsDataStore.store.updateData {
            it.copy(userId = id)
        }
    }
}