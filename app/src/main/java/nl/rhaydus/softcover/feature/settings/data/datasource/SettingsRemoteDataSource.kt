package nl.rhaydus.softcover.feature.settings.data.datasource

import nl.rhaydus.softcover.feature.settings.domain.model.UserProfileData

interface SettingsRemoteDataSource {
    suspend fun getUserIdFromBackend(): Int

    suspend fun getUserProfileData(): UserProfileData
}