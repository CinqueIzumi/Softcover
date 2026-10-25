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
        fun `gives each chapter its fixed number`() {
            // ----- Arrange -----
            val expected = mapOf(
                AtlasChapter.YEAR_AT_A_GLANCE to "01",
                AtlasChapter.SEASONS_AND_RECORDS to "02",
                AtlasChapter.GENRES_AND_HOW_YOU_READ to "03",
                AtlasChapter.TEMPORARY to "99",
            )

            // ----- Act -----
            val result = atlasContentsRows()

            // ----- Assert -----
            result.associate { it.chapter to it.number } shouldBe expected
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

        @Test
        fun `titles each chapter eyebrow with its number and name`() {
            // ----- Arrange -----
            val chapters = AtlasChapter.entries

            // ----- Act -----
            val result = chapters.map { atlasChapterEyebrow(it) }

            // ----- Assert -----
            result shouldBe listOf(
                "01 · A year at a glance",
                "02 · Seasons and records",
                "03 · Genres and how you read",
                "99 · Temporary",
            )
        }
    }
}
