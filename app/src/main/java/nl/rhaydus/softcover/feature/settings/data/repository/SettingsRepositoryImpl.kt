package nl.rhaydus.softcover.feature.settings.data.repository

import nl.rhaydus.softcover.feature.settings.data.datasource.LocalSettingsDataSource
import nl.rhaydus.softcover.feature.settings.domain.repository.SettingsRepository
import javax.inject.Inject

class SettingsRepositoryImpl @Inject constructor(
    private val localSettingsDataSource: LocalSettingsDataSource,
) : SettingsRepository {
    override suspend fun updateApiKey(key: String) {
        localSettingsDataSource.updateApiKey(key = key)
    }

    override suspend fun getApiKey(): String {
        return localSettingsDataSource.getApiKey()
    }
}