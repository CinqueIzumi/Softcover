package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.withContext
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.feature.settings.data.mapper.toBundledPublishedDocument
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocument
import nl.rhaydus.softcover.feature.settings.generated.resources.Res

/**
 * The build-time fallback copies of the published documents, bundled as Compose resources by the
 * `bundlePublishedDocuments` Gradle task (see `feature/settings/build.gradle.kts`) from the repo-root
 * files - never a second hand-maintained copy. Read only until the first live fetch lands.
 */
internal interface PublishedDocumentBundledDataSource {
    suspend fun readBundledDocument(key: PublishedDocumentKey): PublishedDocument
}

internal class PublishedDocumentBundledDataSourceImpl(
    private val appDispatchers: AppDispatchers,
) : PublishedDocumentBundledDataSource {
    override suspend fun readBundledDocument(key: PublishedDocumentKey): PublishedDocument =
        withContext(appDispatchers.io) {
            Res.readBytes(key.bundledPath).decodeToString().toBundledPublishedDocument()
        }
}
