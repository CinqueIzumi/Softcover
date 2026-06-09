package nl.rhaydus.softcover.feature.explore.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioStorage
import okio.FileSystem
import okio.Path
import nl.rhaydus.softcover.feature.explore.data.datastore.serializer.SearchHistoryEntity
import nl.rhaydus.softcover.feature.explore.data.datastore.serializer.SearchHistorySerializer
import kotlin.jvm.JvmInline

@JvmInline
internal value class SearchHistoryDataStore(val store: DataStore<SearchHistoryEntity>)

/**
 * Shared construction site for the search-history DataStore. Persistence is fully multiplatform via
 * okio; the platform-bound pieces are supplied by the platform Koin module — [producePath] (the
 * per-target file location; Android: `filesDir/datastore/search_history.json`, iOS: the documents
 * directory) and [fileSystem] (`FileSystem.SYSTEM`, which okio only exposes on JVM/Native, not in
 * `commonMain`). The Android path matches the previous `dataStore(fileName = ...)` location, so
 * existing stores are picked up unchanged.
 */
internal fun createSearchHistoryDataStore(
    fileSystem: FileSystem,
    producePath: () -> Path,
): DataStore<SearchHistoryEntity> =
    DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = fileSystem,
            serializer = SearchHistorySerializer,
            producePath = producePath,
        ),
    )
