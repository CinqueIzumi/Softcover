package nl.rhaydus.softcover.core.profile.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.core.profile.data.datastore.ProfileCacheDataStore
import nl.rhaydus.softcover.core.profile.data.model.toEntity
import nl.rhaydus.softcover.core.profile.data.model.toModel
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData

interface ProfileLocalDataSource {
    fun observeUserProfileData(): Flow<UserProfileData?>

    suspend fun cacheUserProfileData(data: UserProfileData)

    suspend fun clear()
}

class ProfileLocalDataSourceImpl(
    private val profileCacheDataStore: ProfileCacheDataStore,
) : ProfileLocalDataSource {
    override fun observeUserProfileData(): Flow<UserProfileData?> =
        profileCacheDataStore.store.data
            .map { it.profile?.toModel() }
            .distinctUntilChanged()

    override suspend fun cacheUserProfileData(data: UserProfileData) {
        profileCacheDataStore.store.updateData {
            it.copy(profile = data.toEntity())
        }
    }

    override suspend fun clear() {
        profileCacheDataStore.store.updateData {
            it.copy(profile = null)
        }
    }
}
