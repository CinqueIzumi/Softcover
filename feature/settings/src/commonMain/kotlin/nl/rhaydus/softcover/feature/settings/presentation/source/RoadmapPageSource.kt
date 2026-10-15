package nl.rhaydus.softcover.feature.settings.presentation.source

import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocumentSource
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObserveRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.RefreshRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.presentation.mapper.toMarkdownDocumentUiModel
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentPageSnapshot
import nl.rhaydus.softcover.feature.settings.presentation.model.DocumentSection

/** The roadmap is one continuous document, so it maps to a single section with no header or date. */
internal class RoadmapPageSource(
    private val observeRoadmapUseCase: ObserveRoadmapUseCase,
    private val refreshRoadmapUseCase: RefreshRoadmapUseCase,
) : DocumentPageSource {
    override fun observe(): Flow<DocumentPageSnapshot> = observeRoadmapUseCase().map { document ->
        DocumentPageSnapshot(
            sections = persistentListOf(
                DocumentSection(
                    header = null,
                    dateText = null,
                    body = document.blocks.toMarkdownDocumentUiModel(),
                ),
            ),
            isBundled = document.source == PublishedDocumentSource.BUNDLED,
            fetchedAtEpochMillis = document.fetchedAtEpochMillis,
        )
    }

    override suspend fun refresh(force: Boolean): Result<Unit> = refreshRoadmapUseCase(force = force)
}
