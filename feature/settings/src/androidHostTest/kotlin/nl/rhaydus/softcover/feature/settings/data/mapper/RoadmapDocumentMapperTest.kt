package nl.rhaydus.softcover.feature.settings.data.mapper

import io.kotest.matchers.shouldBe

import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

import nl.rhaydus.softcover.core.database.model.RoadmapDocumentEntity
import nl.rhaydus.softcover.feature.settings.domain.model.RoadmapSource

class RoadmapDocumentMapperTest {
    @Nested
    inner class ToRoadmapDocument {
        @Test
        fun `maps entity markdown and timestamp into a cache-sourced document`() {
            // ----- Arrange -----
            val markdown = "# Title\n\nBody text"
            val entity = RoadmapDocumentEntity(
                markdown = markdown,
                fetchedAtEpochMillis = 12345L,
            )

            // ----- Act -----
            val document = entity.toRoadmapDocument()

            // ----- Assert -----
            document.source shouldBe RoadmapSource.CACHE
            document.fetchedAtEpochMillis shouldBe 12345L
            document.blocks shouldBe markdown.toRoadmapBlocks()
        }
    }

    @Nested
    inner class ToBundledRoadmapDocument {
        @Test
        fun `maps raw markdown into a bundled document with no timestamp`() {
            // ----- Arrange -----
            val markdown = "# Title\n\nBody text"

            // ----- Act -----
            val document = markdown.toBundledRoadmapDocument()

            // ----- Assert -----
            document.source shouldBe RoadmapSource.BUNDLED
            document.fetchedAtEpochMillis shouldBe null
            document.blocks shouldBe markdown.toRoadmapBlocks()
        }
    }
}
