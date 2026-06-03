package nl.rhaydus.softcover.core.profile.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import nl.rhaydus.softcover.core.profile.data.datastore.serializer.ProfileCacheSerializer
import nl.rhaydus.softcover.core.profile.data.model.ProfileCacheEntity

@JvmInline
internal value class ProfileCacheDataStore(val store: DataStore<ProfileCacheEntity>)

/**
 * Single platform-specific construction site for the profile-cache DataStore. Only the file location
 * is Android-bound, so a future KMP split just supplies the `produceFile` path per target. The path
 * (`filesDir/datastore/profile_cache.json`) matches the previous `dataStore(...)` delegate, so an
 * existing cache is picked up unchanged.
 */
internal fun createProfileCacheDataStore(context: Context): DataStore<ProfileCacheEntity> =
    DataStoreFactory.create(
        serializer = ProfileCacheSerializer,
        produceFile = { context.dataStoreFile("profile_cache.json") },
    )
