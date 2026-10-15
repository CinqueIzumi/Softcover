package nl.rhaydus.softcover.feature.settings.presentation.source

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownBlock
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownSpan
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocument
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocumentSource
import nl.rhaydus.softcover.feature.settings.domain.usecase.ObserveRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.domain.usecase.RefreshRoadmapUseCase
import nl.rhaydus.softcover.feature.settings.presentation.mapper.toMarkdownDocumentUiModel
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

private val TEST_DOCUMENT = PublishedDocument(
    blocks = persistentListOf(
        MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "One"))),
        MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Two"))),
    ),
    fetchedAtEpochMillis = 1_000L,
    source = PublishedDocumentSource.CACHE,
)

class RoadmapPageSourceTest {
    private lateinit var observeRoadmapUseCase: ObserveRoadmapUseCase
    private lateinit var refreshRoadmapUseCase: RefreshRoadmapUseCase
    private lateinit var source: RoadmapPageSource

    @BeforeEach
    fun setUp() {
        observeRoadmapUseCase = mockk()
        refreshRoadmapUseCase = mockk()
        source = RoadmapPageSource(
            observeRoadmapUseCase = observeRoadmapUseCase,
            refreshRoadmapUseCase = refreshRoadmapUseCase,
        )
    }

    @Nested
    inner class Observe {
        @Test
        fun `maps the whole document to one header-less section`() = runTest {
            // ----- Arrange -----
            every {
                observeRoadmapUseCase()
            } returns flowOf(TEST_DOCUMENT)

            // ----- Act -----
            val snapshot = source.observe().first()

            // ----- Assert -----
            snapshot.sections shouldHaveSize 1
            val section = snapshot.sections.single()
            section.header.shouldBeNull()
            section.dateText.shouldBeNull()
            section.body shouldBe TEST_DOCUMENT.blocks.toMarkdownDocumentUiModel()
        }

        @Test
        fun `an empty document still maps to a single section`() = runTest {
            // ----- Arrange -----
            every {
                observeRoadmapUseCase()
            } returns flowOf(TEST_DOCUMENT.copy(blocks = persistentListOf()))

            // ----- Act -----
            val snapshot = source.observe().first()

            // ----- Assert -----
            snapshot.sections shouldHaveSize 1
        }

        @Test
        fun `carries the bundled flag and fetch time through`() = runTest {
            // ----- Arrange -----
            every {
                observeRoadmapUseCase()
            } returns flowOf(
                TEST_DOCUMENT.copy(
                    source = PublishedDocumentSource.BUNDLED,
                    fetchedAtEpochMillis = null,
                ),
                TEST_DOCUMENT,
            )

            // ----- Act -----
            val snapshots = mutableListOf<Pair<Boolean, Long?>>()
            source.observe().collect { snapshots.add(it.isBundled to it.fetchedAtEpochMillis) }

            // ----- Assert -----
            snapshots shouldBe listOf(true to null, false to 1_000L)
        }
    }
}
