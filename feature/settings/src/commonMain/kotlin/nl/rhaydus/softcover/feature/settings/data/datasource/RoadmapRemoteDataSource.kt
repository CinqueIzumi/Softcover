package nl.rhaydus.softcover.feature.settings.data.datasource

import com.apollographql.apollo.network.http.HttpEngine
import kotlinx.coroutines.withContext
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.core.network.helper.safeGetText

/**
 * The published `ROADMAP.md`, read straight from the repo's default branch so a milestone edit reaches
 * readers with no app release. Returns the raw markdown: it is what gets cached, so the stored copy
 * stays re-parseable if the renderer changes.
 */
interface RoadmapRemoteDataSource {
    suspend fun fetchRoadmapMarkdown(): String
}

internal class RoadmapRemoteDataSourceImpl(
    private val httpEngine: HttpEngine,
    private val appDispatchers: AppDispatchers,
) : RoadmapRemoteDataSource {
    override suspend fun fetchRoadmapMarkdown(): String = withContext(appDispatchers.io) {
        httpEngine.safeGetText(url = ROADMAP_RAW_URL)
    }

    private companion object {
        const val ROADMAP_RAW_URL = "https://raw.githubusercontent.com/CinqueIzumi/Softcover/main/ROADMAP.md"
    }
}
