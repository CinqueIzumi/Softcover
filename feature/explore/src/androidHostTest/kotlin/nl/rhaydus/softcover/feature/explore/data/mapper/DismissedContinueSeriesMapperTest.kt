package nl.rhaydus.softcover.feature.explore.data.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesBookEntity
import nl.rhaydus.softcover.core.database.model.DismissedContinueSeriesEntity
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeries
import nl.rhaydus.softcover.feature.explore.domain.model.DismissedSeriesBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class DismissedContinueSeriesMapperTest {
    @Nested
    inner class BookEntityToDomain {
        @Test
        fun `maps every field and renames bookTitle to title`() {
            // ----- Arrange -----
            val entity = DismissedContinueSeriesBookEntity(
                bookId = 7,
                bookTitle = "Dune",
                coverUrl = "https://cover",
                authorText = "Frank Herbert",
                seriesName = "Dune Chronicles",
                seriesId = 3,
                seriesPosition = 1.5,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result shouldBe DismissedSeriesBook(
                bookId = 7,
                title = "Dune",
                coverUrl = "https://cover",
                authorText = "Frank Herbert",
                seriesName = "Dune Chronicles",
                seriesId = 3,
                seriesPosition = 1.5,
            )
        }

        @Test
        fun `keeps absent optional fields null`() {
            // ----- Arrange -----
            val entity = DismissedContinueSeriesBookEntity(bookId = 7)

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.bookId shouldBe 7
            result.title.shouldBeNull()
            result.seriesId.shouldBeNull()
            result.seriesPosition.shouldBeNull()
        }
    }

    @Nested
    inner class BookToEntity {
        @Test
        fun `maps every field and renames title to bookTitle`() {
            // ----- Arrange -----
            val book = DismissedSeriesBook(
                bookId = 9,
                title = "Emma",
                coverUrl = "https://c",
                authorText = "Jane Austen",
                seriesName = "S",
                seriesId = 4,
                seriesPosition = 2.0,
            )

            // ----- Act -----
            val result = book.toEntity()

            // ----- Assert -----
            result shouldBe DismissedContinueSeriesBookEntity(
                bookId = 9,
                bookTitle = "Emma",
                coverUrl = "https://c",
                authorText = "Jane Austen",
                seriesName = "S",
                seriesId = 4,
                seriesPosition = 2.0,
            )
        }

        @Test
        fun `round-trips through the entity unchanged`() {
            // ----- Arrange -----
            val book = DismissedSeriesBook(
                bookId = 1,
                title = null,
                coverUrl = null,
                authorText = "A",
                seriesName = null,
                seriesId = 2,
                seriesPosition = 0.5,
            )

            // ----- Act -----
            val result = book.toEntity().toDomain()

            // ----- Assert -----
            result shouldBe book
        }
    }

    @Nested
    inner class SeriesEntityToDomain {
        @Test
        fun `maps every field`() {
            // ----- Arrange -----
            val entity = DismissedContinueSeriesEntity(
                seriesId = 3,
                seriesName = "Dune Chronicles",
                coverUrl = "https://cover",
                authorText = "Frank Herbert",
                bookCount = 6,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result shouldBe DismissedSeries(
                seriesId = 3,
                seriesName = "Dune Chronicles",
                coverUrl = "https://cover",
                authorText = "Frank Herbert",
                bookCount = 6,
            )
        }

        @Test
        fun `keeps absent optional fields null`() {
            // ----- Arrange -----
            val entity = DismissedContinueSeriesEntity(seriesId = 3)

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.seriesId shouldBe 3
            result.seriesName.shouldBeNull()
            result.bookCount.shouldBeNull()
        }
    }
}
