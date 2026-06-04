package nl.rhaydus.softcover.core.preferences.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import okio.FileSystem
import okio.Path
import nl.rhaydus.softcover.core.preferences.data.datastore.serializer.AppSettingsSerializer
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity
import kotlin.jvm.JvmInline

@JvmInline
internal value class AppSettingsDataStore(val store: DataStore<AppSettingsEntity>)

/**
 * Shared construction site for the app-settings DataStore. Persistence is fully multiplatform via
 * okio; the only platform-bound piece is [producePath] — the per-target file location, supplied by
 * the platform Koin module (Android: `filesDir/datastore/app_settings.json`; iOS: the documents
 * directory). The Android path matches the previous `dataStoreFile(...)` location, so existing
 * stores are picked up unchanged.
 */
internal fun createAppSettingsDataStore(producePath: () -> Path): DataStore<AppSettingsEntity> =
    DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = AppSettingsSerializer,
            producePath = producePath,
        ),
    )
