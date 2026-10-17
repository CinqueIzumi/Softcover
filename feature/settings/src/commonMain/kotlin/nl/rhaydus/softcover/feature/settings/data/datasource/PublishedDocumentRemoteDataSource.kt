package nl.rhaydus.softcover.feature.settings.data.datasource

import com.apollographql.apollo.network.http.HttpEngine
import kotlinx.coroutines.withContext
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.network.helper.safeGetText

/**
 * A document published from the repo's default branch, read straight from there so an edit reaches
 * readers with no app release. Returns the raw markdown: it is what gets cached, so the stored copy
 * stays re-parseable if the renderer changes.
 */
internal interface PublishedDocumentRemoteDataSource {
    suspend fun fetchMarkdown(key: PublishedDocumentKey): String
}

internal class PublishedDocumentRemoteDataSourceImpl(
    private val httpEngine: HttpEngine,
    private val appDispatchers: AppDispatchers,
) : PublishedDocumentRemoteDataSource {
    override suspend fun fetchMarkdown(key: PublishedDocumentKey): String = withContext(appDispatchers.io) {
        httpEngine.safeGetText(url = key.rawUrl)
    }
}
