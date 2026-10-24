package nl.rhaydus.softcover.feature.profile.presentation.mapper

import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.component.header.SectionHeaderUiModel
import nl.rhaydus.softcover.feature.profile.presentation.state.AtlasChapter
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class AtlasContentsMapperTest {
    @Nested
    inner class AtlasContentsHeaderMapping {
        @Test
        fun `names the chapter count, singular for one chapter`() {
            // ----- Arrange -----
            val count = AtlasChapter.entries.size
            val expected = if (count == 1) "One chapter" else "$count chapters"

            // ----- Act -----
            val result = atlasContentsHeader()

            // ----- Assert -----
            (result as SectionHeaderUiModel.Section).headlineText shouldBe expected
        }
    }

    @Nested
    inner class AtlasContentsRowsMapping {
        @Test
        fun `yields one row per chapter in reading order`() {
            // ----- Arrange -----
            val chapters = AtlasChapter.entries

            // ----- Act -----
            val result = atlasContentsRows()

            // ----- Assert -----
            result.map { it.chapter } shouldBe chapters
        }

        @Test
        fun `numbers rows by position as two digits starting at 01`() {
            // ----- Arrange -----
            val expected = AtlasChapter.entries.indices.map { index ->
                (index + 1).toString().padStart(
                    length = 2,
                    padChar = '0',
                )
            }

            // ----- Act -----
            val result = atlasContentsRows()

            // ----- Assert -----
            result.map { it.number } shouldBe expected
        }
    }

    @Nested
    inner class AtlasChapterHeaderMapping {
        @Test
        fun `opens the eyebrow with the number its contents row carries`() {
            // ----- Arrange -----
            val rows = atlasContentsRows()

            // ----- Act -----
            val headers = rows.map { atlasChapterHeader(it.chapter) }

            // ----- Assert -----
            headers.map { it.eyebrow } shouldBe rows.map { "${it.number} · ${it.title}" }
        }
    }
}
