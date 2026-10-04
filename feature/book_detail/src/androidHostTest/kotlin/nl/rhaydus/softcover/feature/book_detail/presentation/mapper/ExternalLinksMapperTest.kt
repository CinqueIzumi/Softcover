package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExternalLinksMapperTest {
    private fun edition(
        isbn10: String?,
        isbn13: String?,
    ): BookEdition = buildEdition(id = 1).copy(
        isbn10 = isbn10,
        isbn13 = isbn13,
    )

    @Nested
    inner class ToExternalLinksUiModel {
        @Test
        fun `builds search urls from the ISBN13 when present`() {
            // ----- Arrange -----
            val edition = edition(
                isbn10 = "0306406152",
                isbn13 = "9780306406157",
            )

            // ----- Act -----
            val result = edition.toExternalLinksUiModel()

            // ----- Assert -----
            result.shouldNotBeNull().links.pills.map { it.key }.map { key -> result.links[key] } shouldBe listOf(
                "https://bookshop.org/search?keywords=9780306406157",
                "https://www.amazon.com/s?k=9780306406157",
                "https://openlibrary.org/search?isbn=9780306406157",
            )
        }

        @Test
        fun `falls back to the ISBN10 when there is no ISBN13`() {
            // ----- Arrange -----
            val edition = edition(
                isbn10 = "0306406152",
                isbn13 = null,
            )

            // ----- Act -----
            val result = edition.toExternalLinksUiModel()

            // ----- Assert -----
            result.shouldNotBeNull().links.pills.map { it.key }.map { key -> result.links[key] } shouldBe listOf(
                "https://bookshop.org/search?keywords=0306406152",
                "https://www.amazon.com/s?k=0306406152",
                "https://openlibrary.org/search?isbn=0306406152",
            )
        }

        @Test
        fun `falls back to the ISBN10 when the ISBN13 is blank`() {
            // ----- Arrange -----
            val edition = edition(
                isbn10 = "0306406152",
                isbn13 = "   ",
            )

            // ----- Act -----
            val result = edition.toExternalLinksUiModel()

            // ----- Assert -----
            result.shouldNotBeNull().links["amazon"] shouldBe "https://www.amazon.com/s?k=0306406152"
        }

        @Test
        fun `is null when both ISBNs are blank`() {
            // ----- Arrange -----
            val edition = edition(
                isbn10 = "",
                isbn13 = " ",
            )

            // ----- Act -----
            val result = edition.toExternalLinksUiModel()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `is null when both ISBNs are missing`() {
            // ----- Arrange -----
            val edition = edition(
                isbn10 = null,
                isbn13 = null,
            )

            // ----- Act -----
            val result = edition.toExternalLinksUiModel()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `is null for a null edition`() {
            // ----- Arrange -----
            val edition: BookEdition? = null

            // ----- Act -----
            val result = edition.toExternalLinksUiModel()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `every pill key resolves to a url`() {
            // ----- Arrange -----
            val edition = edition(
                isbn10 = null,
                isbn13 = "9780306406157",
            )

            // ----- Act -----
            val result = edition.toExternalLinksUiModel().shouldNotBeNull()

            // ----- Assert -----
            result.links.pills.map { it.key }.toSet() shouldBe result.links.payloadByKey.keys
        }
    }
}
