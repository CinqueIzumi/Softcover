package nl.rhaydus.softcover.core.profile.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import okio.FileSystem
import okio.Path
import nl.rhaydus.softcover.core.profile.data.datastore.serializer.ProfileCacheSerializer
import nl.rhaydus.softcover.core.profile.data.model.ProfileCacheEntity
import kotlin.jvm.JvmInline

@JvmInline
internal value class ProfileCacheDataStore(val store: DataStore<ProfileCacheEntity>)

/**
 * Shared construction site for the profile-cache DataStore. Persistence is fully multiplatform via
 * okio; the platform-bound pieces are supplied by the platform Koin module — [producePath] (the
 * per-target file location; Android: `filesDir/datastore/profile_cache.json`, iOS: the documents
 * directory) and [fileSystem] (`FileSystem.SYSTEM`, which okio only exposes on JVM/Native, not in
 * `commonMain`). The Android path matches the previous `dataStoreFile(...)` location, so existing
 * stores are picked up unchanged.
 */
internal fun createProfileCacheDataStore(
    fileSystem: FileSystem,
    producePath: () -> Path,
): DataStore<ProfileCacheEntity> =
    DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = fileSystem,
            serializer = ProfileCacheSerializer,
            producePath = producePath,
        ),
    )
