package nl.rhaydus.softcover.core.preferences.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import okio.FileSystem
import okio.Path
import kotlin.jvm.JvmInline
import nl.rhaydus.softcover.core.preferences.data.datastore.serializer.AppSettingsSerializer
import nl.rhaydus.softcover.core.preferences.data.model.AppSettingsEntity

@JvmInline
internal value class AppSettingsDataStore(val store: DataStore<AppSettingsEntity>)

/**
 * Shared construction site for the app-settings DataStore. Persistence is fully multiplatform via
 * okio; the platform-bound pieces are supplied by the platform Koin module — [producePath] (the
 * per-target file location; Android: `filesDir/datastore/app_settings.json`, iOS: the documents
 * directory) and [fileSystem] (`FileSystem.SYSTEM`, which okio only exposes on JVM/Native, not in
 * `commonMain`). The Android path matches the previous `dataStoreFile(...)` location, so existing
 * stores are picked up unchanged.
 */
internal fun createAppSettingsDataStore(
    fileSystem: FileSystem,
    producePath: () -> Path,
): DataStore<AppSettingsEntity> =
    DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = fileSystem,
            serializer = AppSettingsSerializer,
            producePath = producePath,
        ),
    )
