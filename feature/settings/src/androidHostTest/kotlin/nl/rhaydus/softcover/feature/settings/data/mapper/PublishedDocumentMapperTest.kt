package nl.rhaydus.softcover.feature.settings.data.mapper

import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.core.database.model.PublishedDocumentEntity
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownBlock
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownSpan
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocumentSource
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class PublishedDocumentMapperTest {
    @Nested
    inner class ToPublishedDocument {
        @Test
        fun `maps entity markdown and timestamp into a cache-sourced document`() {
            // ----- Arrange -----
            val markdown = "# Title\n\nBody text"
            val entity = PublishedDocumentEntity(
                key = "roadmap",
                markdown = markdown,
                fetchedAtEpochMillis = 12345L,
            )

            // ----- Act -----
            val document = entity.toPublishedDocument()

            // ----- Assert -----
            document.source shouldBe PublishedDocumentSource.CACHE
            document.fetchedAtEpochMillis shouldBe 12345L
            document.blocks shouldBe persistentListOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Body text"))),
            )
        }
    }

    @Nested
    inner class ToBundledPublishedDocument {
        @Test
        fun `maps raw markdown into a bundled document with no timestamp`() {
            // ----- Arrange -----
            val markdown = "# Title\n\nBody text"

            // ----- Act -----
            val document = markdown.toBundledPublishedDocument()

            // ----- Assert -----
            document.source shouldBe PublishedDocumentSource.BUNDLED
            document.fetchedAtEpochMillis shouldBe null
            document.blocks shouldBe persistentListOf(
                MarkdownBlock.Paragraph(spans = persistentListOf(MarkdownSpan(text = "Body text"))),
            )
        }
    }
}
