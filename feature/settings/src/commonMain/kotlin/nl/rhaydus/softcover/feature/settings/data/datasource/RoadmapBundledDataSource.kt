package nl.rhaydus.softcover.feature.settings.data.datasource

import kotlinx.coroutines.withContext
import nl.rhaydus.common.AppDispatchers
import nl.rhaydus.softcover.feature.settings.data.mapper.toBundledRoadmapDocument
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.softcover.feature.settings.generated.resources.Res

/**
 * The build-time fallback copy of `ROADMAP.md`, bundled as a Compose resource by the
 * `bundleRoadmapResource` Gradle task (see `feature/settings/build.gradle.kts`) from the repo-root
 * file - never a second hand-maintained copy. Read only until the first live fetch lands.
 */
interface RoadmapBundledDataSource {
    suspend fun readBundledRoadmap(): RoadmapDocument
}

internal class RoadmapBundledDataSourceImpl(
    private val appDispatchers: AppDispatchers,
) : RoadmapBundledDataSource {
    override suspend fun readBundledRoadmap(): RoadmapDocument = withContext(appDispatchers.io) {
        Res.readBytes(BUNDLED_ROADMAP_PATH).decodeToString().toBundledRoadmapDocument()
    }

    private companion object {
        const val BUNDLED_ROADMAP_PATH = "files/roadmap.md"
    }
}
