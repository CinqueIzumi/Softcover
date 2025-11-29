package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.flow.first
import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore
import javax.inject.Inject

class SettingsLocalDataSourceImpl @Inject constructor(
    private val appSettingsDataStore: AppSettingsDataStore,
) : SettingsLocalDataSource {
    override suspend fun updateApiKey(key: String) {
        appSettingsDataStore.updateData {
            it.copy(apiKey = key)
        }
    }

    override suspend fun getApiKey(): String {
        return appSettingsDataStore.data.first().apiKey
    }

    override suspend fun getUserId(): Int {
        return appSettingsDataStore.data.first().userId
    }

    override suspend fun updateUserId(id: Int) {
        appSettingsDataStore.updateData {
            it.copy(userId = id)
        }
    }
}