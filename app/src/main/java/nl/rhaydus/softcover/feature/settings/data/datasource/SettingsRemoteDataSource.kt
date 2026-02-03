package nl.rhaydus.softcover.feature.settings.data.datasource

import nl.rhaydus.softcover.feature.settings.domain.model.UserInformation

interface SettingsRemoteDataSource {
    suspend fun getUserInformation(): UserInformation
} 