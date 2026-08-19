package nl.rhaydus.softcover.feature.settings.data.mapper

import nl.rhaydus.softcover.core.database.model.RoadmapDocumentEntity
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapDocument
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSource

internal fun RoadmapDocumentEntity.toRoadmapDocument(): RoadmapDocument = RoadmapDocument(
    blocks = markdown.toRoadmapBlocks(),
    fetchedAtEpochMillis = fetchedAtEpochMillis,
    source = RoadmapSource.CACHE,
)

internal fun String.toBundledRoadmapDocument(): RoadmapDocument = RoadmapDocument(
    blocks = toRoadmapBlocks(),
    fetchedAtEpochMillis = null,
    source = RoadmapSource.BUNDLED,
)
