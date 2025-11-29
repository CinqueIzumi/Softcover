package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.flow.first
import nl.rhaydus.softcover.feature.settings.data.datastore.AppSettingsDataStore
import javax.inject.Inject

class LocalSettingsDataSourceImpl @Inject constructor(
    private val appSettingsDataStore: AppSettingsDataStore,
) : LocalSettingsDataSource {
    override suspend fun updateApiKey(key: String) {
        appSettingsDataStore.updateData {
            it.copy(apiKey = key)
        }
    }

    override suspend fun getApiKey(): String {
        return appSettingsDataStore.data.first().apiKey
    }
}