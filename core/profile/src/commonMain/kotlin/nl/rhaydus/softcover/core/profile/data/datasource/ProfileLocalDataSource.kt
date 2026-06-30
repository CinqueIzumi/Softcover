package nl.rhaydus.softcover.core.profile.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.core.profile.data.datastore.ProfileCacheDataStore
import nl.rhaydus.softcover.core.profile.data.model.toEntity
import nl.rhaydus.softcover.core.profile.data.model.toModel
import nl.rhaydus.softcover.core.profile.domain.model.UserProfileData

interface ProfileLocalDataSource {
    fun observeUserProfileData(): Flow<UserProfileData?>

    suspend fun cacheUserProfileData(data: UserProfileData)

    suspend fun markActiveReadingDate(date: LocalDate)

    suspend fun clear()
}

internal class ProfileLocalDataSourceImpl(
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

    override suspend fun markActiveReadingDate(date: LocalDate) {
        val isoDate = date.toString()

        profileCacheDataStore.store.updateData { cache ->
            // No cached profile yet → nothing to merge into; the next server refresh will carry it.
            val profile = cache.profile ?: return@updateData cache

            if (isoDate in profile.activeReadingDates) return@updateData cache

            cache.copy(
                profile = profile.copy(
                    activeReadingDates = (profile.activeReadingDates + isoDate).sorted(),
                ),
            )
        }
    }

    override suspend fun clear() {
        profileCacheDataStore.store.updateData {
            it.copy(profile = null)
        }
    }
}
