package nl.rhaydus.softcover.feature.settings.data.mapper

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import kotlinx.datetime.LocalDate
import nl.rhaydus.softcover.feature.settings.domain.model.MarkdownBlock
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocument
import nl.rhaydus.softcover.feature.settings.domain.model.PublishedDocumentSource
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ChangelogMapperTest {
    private fun document(
        markdown: String,
        fetchedAtEpochMillis: Long? = null,
        source: PublishedDocumentSource = PublishedDocumentSource.CACHE,
    ): PublishedDocument = PublishedDocument(
        blocks = markdown.trimIndent().toMarkdownBlocks(),
        fetchedAtEpochMillis = fetchedAtEpochMillis,
        source = source,
    )

    private fun MarkdownBlock.plainText(): String = when (this) {
        is MarkdownBlock.Heading -> spans.joinToString("") { it.text }
        is MarkdownBlock.Paragraph -> spans.joinToString("") { it.text }
        is MarkdownBlock.BulletListItem -> spans.joinToString("") { it.text }
        is MarkdownBlock.OrderedListItem -> spans.joinToString("") { it.text }
        is MarkdownBlock.BlockQuote -> spans.joinToString("") { it.text }
        MarkdownBlock.ThematicBreak -> "---"
    }

    @Nested
    inner class EntrySplitting {
        @Test
        fun `splits the document at each version headline heading in file order`() {
            // ----- Arrange -----
            val document = document(
                """
                # Softcover changelog

                ## 3.2.0: Fresh shelves

                - Added shelves

                ## 3.1.0: Quieter sync

                - Fixed sync
                """,
            )

            // ----- Act -----
            val changelog = document.toChangelog()

            // ----- Assert -----
            changelog.entries.map { it.version } shouldBe listOf("3.2.0", "3.1.0")
            changelog.entries.map { it.headline } shouldBe listOf("Fresh shelves", "Quieter sync")
            changelog.entries[0].blocks.map { it.plainText() } shouldBe listOf("Added shelves")
            changelog.entries[1].blocks.map { it.plainText() } shouldBe listOf("Fixed sync")
        }

        @Test
        fun `trims the version and headline`() {
            // ----- Arrange -----
            val document = document("##   3.2.0:    Fresh shelves   ")

            // ----- Act -----
            val entry = document.toChangelog().entries.single()

            // ----- Assert -----
            entry.version shouldBe "3.2.0"
            entry.headline shouldBe "Fresh shelves"
        }

        @Test
        fun `drops the blocks before the first entry`() {
            // ----- Arrange -----
            val document = document(
                """
                # Softcover changelog

                Intro paragraph that belongs to no release.

                ## 3.2.0: Fresh shelves

                - Added shelves
                """,
            )

            // ----- Act -----
            val changelog = document.toChangelog()

            // ----- Assert -----
            changelog.entries shouldHaveSize 1
            changelog.entries.single().blocks.map { it.plainText() } shouldBe listOf("Added shelves")
        }

        @Test
        fun `keeps a level three heading inside the entry blocks`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                ### Fixes

                - Fixed sync
                """,
            )

            // ----- Act -----
            val entry = document.toChangelog().entries.single()

            // ----- Assert -----
            entry.blocks shouldHaveSize 2
            (entry.blocks[0] as MarkdownBlock.Heading).level shouldBe 3
        }

        @Test
        fun `yields no entries for an empty document`() {
            // ----- Arrange -----
            val document = document("")

            // ----- Act -----
            val changelog = document.toChangelog()

            // ----- Assert -----
            changelog.entries.shouldBeEmpty()
        }

        @Test
        fun `yields an entry with no blocks when nothing follows its heading`() {
            // ----- Arrange -----
            val document = document("## 3.2.0: Fresh shelves")

            // ----- Act -----
            val entry = document.toChangelog().entries.single()

            // ----- Assert -----
            entry.blocks.shouldBeEmpty()
            entry.releaseDate.shouldBeNull()
        }
    }

    @Nested
    inner class MalformedHeadings {
        @Test
        fun `a level two heading without a colon separator ends the release and drops its content`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                - Added shelves

                ## Upcoming

                - Dropped content
                """,
            )

            // ----- Act -----
            val changelog = document.toChangelog()

            // ----- Assert -----
            changelog.entries shouldHaveSize 1
            changelog.entries.single().blocks.map { it.plainText() } shouldBe listOf("Added shelves")
        }

        @Test
        fun `a level two heading with a blank headline ends the release and drops its content`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                - Added shelves

                ## 3.1.0:${' '}

                - Dropped content
                """,
            )

            // ----- Act -----
            val changelog = document.toChangelog()

            // ----- Assert -----
            changelog.entries shouldHaveSize 1
            changelog.entries.single().blocks.map { it.plainText() } shouldBe listOf("Added shelves")
        }

        @Test
        fun `a level two heading with a blank version ends the release and drops its content`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                - Added shelves

                ##  : Mystery

                - Dropped content
                """,
            )

            // ----- Act -----
            val changelog = document.toChangelog()

            // ----- Assert -----
            changelog.entries shouldHaveSize 1
            changelog.entries.single().blocks.map { it.plainText() } shouldBe listOf("Added shelves")
        }

        @Test
        fun `a level two heading whose version does not start with a digit ends the release and drops its content`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                - Added shelves

                ## Notes: Things to know

                - Dropped content
                """,
            )

            // ----- Act -----
            val changelog = document.toChangelog()

            // ----- Assert -----
            changelog.entries shouldHaveSize 1
            changelog.entries.single().blocks.map { it.plainText() } shouldBe listOf("Added shelves")
        }

        @Test
        fun `content after a malformed section resumes at the next valid heading`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                - Added shelves

                ## Upcoming

                - Dropped content

                ### Dropped subheading

                ## 3.1.0: Quieter sync

                - Fixed sync
                """,
            )

            // ----- Act -----
            val entries = document.toChangelog().entries

            // ----- Assert -----
            entries.map { it.version } shouldBe listOf("3.2.0", "3.1.0")
            entries[0].blocks.map { it.plainText() } shouldBe listOf("Added shelves")
            entries[1].blocks.map { it.plainText() } shouldBe listOf("Fixed sync")
        }

        @Test
        fun `a level three heading with a version shape stays entry content`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                ### 3.1.0: Not a release

                - Added shelves
                """,
            )

            // ----- Act -----
            val entry = document.toChangelog().entries.single()

            // ----- Assert -----
            entry.version shouldBe "3.2.0"
            entry.blocks shouldHaveSize 2
            (entry.blocks[0] as MarkdownBlock.Heading).level shouldBe 3
        }

        @Test
        fun `a document of only malformed headings yields no entries`() {
            // ----- Arrange -----
            val document = document("## Upcoming")

            // ----- Act -----
            val changelog = document.toChangelog()

            // ----- Assert -----
            changelog.entries.shouldBeEmpty()
        }
    }

    @Nested
    inner class ReleaseDate {
        @Test
        fun `an italic ISO date paragraph becomes the release date and leaves the blocks`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                *2026-03-14*

                - Added shelves
                """,
            )

            // ----- Act -----
            val entry = document.toChangelog().entries.single()

            // ----- Assert -----
            entry.releaseDate shouldBe LocalDate(
                year = 2026,
                month = 3,
                day = 14,
            )
            entry.blocks.map { it.plainText() } shouldBe listOf("Added shelves")
        }

        @Test
        fun `a non-italic ISO date paragraph is kept and yields no release date`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                2026-03-14
                """,
            )

            // ----- Act -----
            val entry = document.toChangelog().entries.single()

            // ----- Assert -----
            entry.releaseDate.shouldBeNull()
            entry.blocks.map { it.plainText() } shouldBe listOf("2026-03-14")
        }

        @Test
        fun `an italic paragraph that is not an ISO date is kept and yields no release date`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                *March 14th*
                """,
            )

            // ----- Act -----
            val entry = document.toChangelog().entries.single()

            // ----- Assert -----
            entry.releaseDate.shouldBeNull()
            entry.blocks.map { it.plainText() } shouldBe listOf("March 14th")
        }

        @Test
        fun `an impossible date yields no release date without throwing and keeps the paragraph`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                *2026-13-40*
                """,
            )

            // ----- Act -----
            val entry = document.toChangelog().entries.single()

            // ----- Assert -----
            entry.releaseDate.shouldBeNull()
            entry.blocks.map { it.plainText() } shouldBe listOf("2026-13-40")
        }

        @Test
        fun `only the first block is considered for the release date`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                - Added shelves

                *2026-03-14*
                """,
            )

            // ----- Act -----
            val entry = document.toChangelog().entries.single()

            // ----- Assert -----
            entry.releaseDate.shouldBeNull()
            entry.blocks shouldHaveSize 2
        }

        @Test
        fun `each entry resolves its own release date`() {
            // ----- Arrange -----
            val document = document(
                """
                ## 3.2.0: Fresh shelves

                *2026-03-14*

                ## 3.1.0: Quieter sync

                - Fixed sync
                """,
            )

            // ----- Act -----
            val entries = document.toChangelog().entries

            // ----- Assert -----
            entries[0].releaseDate shouldBe LocalDate(
                year = 2026,
                month = 3,
                day = 14,
            )
            entries[1].releaseDate.shouldBeNull()
        }
    }

    @Nested
    inner class Metadata {
        @Test
        fun `carries the fetch time and source through`() {
            // ----- Arrange -----
            val document = document(
                markdown = "## 3.2.0: Fresh shelves",
                fetchedAtEpochMillis = 9_000L,
                source = PublishedDocumentSource.CACHE,
            )

            // ----- Act -----
            val changelog = document.toChangelog()

            // ----- Assert -----
            changelog.fetchedAtEpochMillis shouldBe 9_000L
            changelog.source shouldBe PublishedDocumentSource.CACHE
        }

        @Test
        fun `carries a bundled source with no fetch time through`() {
            // ----- Arrange -----
            val document = document(
                markdown = "",
                fetchedAtEpochMillis = null,
                source = PublishedDocumentSource.BUNDLED,
            )

            // ----- Act -----
            val changelog = document.toChangelog()

            // ----- Assert -----
            changelog.fetchedAtEpochMillis.shouldBeNull()
            changelog.source shouldBe PublishedDocumentSource.BUNDLED
        }
    }
}
