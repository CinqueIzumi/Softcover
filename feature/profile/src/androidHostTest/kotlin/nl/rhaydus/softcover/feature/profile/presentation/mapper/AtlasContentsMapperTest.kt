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
        fun `counts the chapters it is given`() {
            // ----- Arrange -----
            val chapters = listOf(
                AtlasChapter.YEAR_AT_A_GLANCE,
                AtlasChapter.SEASONS_AND_RECORDS,
                AtlasChapter.GENRES_AND_HOW_YOU_READ,
            )

            // ----- Act -----
            val result = atlasContentsHeader(chapters)

            // ----- Assert -----
            (result as SectionHeaderUiModel.Section).headlineText shouldBe "3 chapters"
        }

        @Test
        fun `is singular for one chapter`() {
            // ----- Arrange -----
            val chapters = listOf(AtlasChapter.YEAR_AT_A_GLANCE)

            // ----- Act -----
            val result = atlasContentsHeader(chapters)

            // ----- Assert -----
            (result as SectionHeaderUiModel.Section).headlineText shouldBe "One chapter"
        }
    }

    @Nested
    inner class AtlasContentsRowsMapping {
        @Test
        fun `yields one row per given chapter in the given order`() {
            // ----- Arrange -----
            val chapters = listOf(AtlasChapter.GENRES_AND_HOW_YOU_READ, AtlasChapter.YEAR_AT_A_GLANCE)

            // ----- Act -----
            val result = atlasContentsRows(chapters)

            // ----- Assert -----
            result.map { it.chapter } shouldBe chapters
        }

        @Test
        fun `keeps a chapter's number when a neighbour is left out`() {
            // ----- Arrange -----
            val all = atlasContentsRows().associate { it.chapter to it.number }
            val chapters = AtlasChapter.entries - AtlasChapter.IN_YOUR_WORDS

            // ----- Act -----
            val result = atlasContentsRows(chapters)

            // ----- Assert -----
            result.associate { it.chapter to it.number } shouldBe all - AtlasChapter.IN_YOUR_WORDS
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
