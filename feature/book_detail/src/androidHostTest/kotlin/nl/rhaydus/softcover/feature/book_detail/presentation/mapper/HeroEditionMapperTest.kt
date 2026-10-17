package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.ReadingFormat
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class HeroEditionMapperTest {
    @Nested
    inner class ToHeroEditionUiModel {
        @Test
        fun `shows the page count for a paged edition`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(pages = 320)

            // ----- Act -----
            val result = edition.toHeroEditionUiModel()

            // ----- Assert -----
            result.length shouldBe "320"
            result.isAudiobook shouldBe false
        }

        @Test
        fun `shows hours and minutes for an audiobook`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                readingFormat = ReadingFormat.Audio,
                audioSeconds = 3_900,
                pages = 320,
            )

            // ----- Act -----
            val result = edition.toHeroEditionUiModel()

            // ----- Assert -----
            result.length shouldBe "1h 5m"
            result.isAudiobook shouldBe true
        }

        @Test
        fun `has no length when the page count is zero`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(pages = 0)

            // ----- Act -----
            val result = edition.toHeroEditionUiModel()

            // ----- Assert -----
            result.length.shouldBeNull()
        }

        @Test
        fun `has no length when the page count is missing`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(pages = null)

            // ----- Act -----
            val result = edition.toHeroEditionUiModel()

            // ----- Assert -----
            result.length.shouldBeNull()
        }

        @Test
        fun `has no length when an audiobook has zero seconds`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                readingFormat = ReadingFormat.Audio,
                audioSeconds = 0,
                pages = 320,
            )

            // ----- Act -----
            val result = edition.toHeroEditionUiModel()

            // ----- Assert -----
            result.length.shouldBeNull()
        }

        @Test
        fun `has no length when an audiobook has no duration even if it lists pages`() {
            // ----- Arrange -----
            val edition = buildEdition(id = 1).copy(
                readingFormat = ReadingFormat.Audio,
                audioSeconds = null,
                pages = 320,
            )

            // ----- Act -----
            val result = edition.toHeroEditionUiModel()

            // ----- Assert -----
            result.length.shouldBeNull()
        }

        @Test
        fun `joins the authors into one string`() {
            // ----- Arrange -----
            val edition = buildEdition(
                id = 1,
                authors = listOf(Author(
                    id = 1,
                    name = "Ann",
                ), Author(
                    id = 2,
                    name = "Bo",
                ),),
            )

            // ----- Act -----
            val result = edition.toHeroEditionUiModel()

            // ----- Assert -----
            result.authors shouldBe "Ann, Bo"
        }
    }
}
