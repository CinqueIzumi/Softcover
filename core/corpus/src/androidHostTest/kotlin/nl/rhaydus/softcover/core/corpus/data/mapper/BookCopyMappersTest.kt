package nl.rhaydus.softcover.core.corpus.data.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.database.model.BookCopyEntity
import nl.rhaydus.softcover.core.domain.model.AcquisitionKind
import nl.rhaydus.softcover.core.domain.model.AcquisitionSource
import nl.rhaydus.softcover.core.domain.model.BookCopy
import nl.rhaydus.softcover.core.domain.model.PricePaid
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookCopyMappersTest {
    @Nested
    inner class RoundTrip {
        @Test
        fun `every preset survives toEntity then toDomain`() {
            AcquisitionKind.entries.forEach { kind ->
                // ----- Arrange -----
                val copy = BookCopy(
                    bookId = 7,
                    source = AcquisitionSource.Preset(kind = kind),
                )

                // ----- Act -----
                val result = copy.toEntity(updatedAt = 5L).toDomain()

                // ----- Assert -----
                result shouldBe copy
            }
        }

        @Test
        fun `an other source survives toEntity then toDomain`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 7,
                source = AcquisitionSource.Other(text = "Uncle's attic"),
            )

            // ----- Act -----
            val result = copy.toEntity(updatedAt = 5L).toDomain()

            // ----- Assert -----
            result shouldBe copy
        }

        @Test
        fun `a price survives toEntity then toDomain`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 7,
                price = PricePaid(
                    amountThousandths = 12_990L,
                    currencyCode = "EUR",
                ),
            )

            // ----- Act -----
            val result = copy.toEntity(updatedAt = 5L).toDomain()

            // ----- Assert -----
            result shouldBe copy
        }
    }

    @Nested
    inner class ToDomain {
        @Test
        fun `a non-blank custom source reads as other and wins over a stored preset`() {
            // ----- Arrange -----
            val entity = BookCopyEntity(
                bookId = 7,
                source = "gift",
                customSource = "Book swap",
                updatedAt = 1L,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.source shouldBe AcquisitionSource.Other(text = "Book swap")
        }

        @Test
        fun `a blank custom source falls back to the stored preset`() {
            // ----- Arrange -----
            val entity = BookCopyEntity(
                bookId = 7,
                source = "borrowed",
                customSource = "   ",
                updatedAt = 1L,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.source shouldBe AcquisitionSource.Preset(kind = AcquisitionKind.BORROWED)
        }

        @Test
        fun `an unknown stored name reads as no source`() {
            // ----- Arrange -----
            val entity = BookCopyEntity(
                bookId = 7,
                source = "stolen",
                updatedAt = 1L,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.source.shouldBeNull()
        }

        @Test
        fun `no stored source reads as no source`() {
            // ----- Arrange -----
            val entity = BookCopyEntity(
                bookId = 7,
                updatedAt = 1L,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result shouldBe BookCopy(bookId = 7)
        }

        @Test
        fun `a stored amount without a currency reads as no price`() {
            // ----- Arrange -----
            val entity = BookCopyEntity(
                bookId = 7,
                updatedAt = 1L,
                priceThousandths = 5_000L,
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.price.shouldBeNull()
        }

        @Test
        fun `a stored currency without an amount reads as no price`() {
            // ----- Arrange -----
            val entity = BookCopyEntity(
                bookId = 7,
                updatedAt = 1L,
                priceCurrency = "USD",
            )

            // ----- Act -----
            val result = entity.toDomain()

            // ----- Assert -----
            result.price.shouldBeNull()
        }
    }

    @Nested
    inner class ToEntity {
        @Test
        fun `other text is trimmed and the preset column is cleared`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 7,
                source = AcquisitionSource.Other(text = "  Book swap \n"),
            )

            // ----- Act -----
            val result = copy.toEntity(updatedAt = 9L)

            // ----- Assert -----
            result shouldBe BookCopyEntity(
                bookId = 7,
                source = null,
                customSource = "Book swap",
                updatedAt = 9L,
            )
        }

        @Test
        fun `a preset clears the custom source`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 7,
                source = AcquisitionSource.Preset(kind = AcquisitionKind.BOUGHT_SECOND_HAND),
            )

            // ----- Act -----
            val result = copy.toEntity(updatedAt = 9L)

            // ----- Assert -----
            result shouldBe BookCopyEntity(
                bookId = 7,
                source = "bought_second_hand",
                customSource = null,
                updatedAt = 9L,
            )
        }

        @Test
        fun `a whitespace-only other source maps to no source`() {
            // ----- Arrange -----
            val copy = BookCopy(
                bookId = 7,
                source = AcquisitionSource.Other(text = " \t "),
            )

            // ----- Act -----
            val result = copy.toEntity(updatedAt = 9L)

            // ----- Assert -----
            result shouldBe BookCopyEntity(
                bookId = 7,
                source = null,
                customSource = null,
                updatedAt = 9L,
            )
        }
    }
}
