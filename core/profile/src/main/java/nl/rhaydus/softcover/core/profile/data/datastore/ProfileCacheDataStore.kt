package nl.rhaydus.softcover.core.profile.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import nl.rhaydus.softcover.core.profile.data.datastore.serializer.ProfileCacheSerializer
import nl.rhaydus.softcover.core.profile.data.model.ProfileCacheEntity

@JvmInline
internal value class ProfileCacheDataStore(val store: DataStore<ProfileCacheEntity>)

internal val Context.profileCache by dataStore(
    fileName = "profile_cache.json",
    serializer = ProfileCacheSerializer,
)
