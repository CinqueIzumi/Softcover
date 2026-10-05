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
    inner class ToExternalLinkPills {
        @Test
        fun `builds the bookshop, amazon and open-library pills when the edition has an ISBN`() {
            // ----- Arrange -----
            val edition = edition(
                isbn10 = "0306406152",
                isbn13 = "9780306406157",
            )

            // ----- Act -----
            val result = edition.toExternalLinkPills()

            // ----- Assert -----
            result.shouldNotBeNull().map { it.key } shouldBe listOf(BOOKSHOP_KEY, AMAZON_KEY, OPEN_LIBRARY_KEY)
        }

        @Test
        fun `searchIsbn prefers the ISBN13`() {
            // ----- Arrange -----
            val edition = edition(
                isbn10 = "0306406152",
                isbn13 = "9780306406157",
            )

            // ----- Act -----
            val result = edition.searchIsbn

            // ----- Assert -----
            result shouldBe "9780306406157"
        }

        @Test
        fun `searchIsbn falls back to the ISBN10 when there is no ISBN13`() {
            // ----- Arrange -----
            val edition = edition(
                isbn10 = "0306406152",
                isbn13 = null,
            )

            // ----- Act -----
            val result = edition.searchIsbn

            // ----- Assert -----
            result shouldBe "0306406152"
        }

        @Test
        fun `searchIsbn falls back to the ISBN10 when the ISBN13 is blank`() {
            // ----- Arrange -----
            val edition = edition(
                isbn10 = "0306406152",
                isbn13 = "   ",
            )

            // ----- Act -----
            val result = edition.searchIsbn

            // ----- Assert -----
            result shouldBe "0306406152"
        }

        @Test
        fun `is null when both ISBNs are missing`() {
            // ----- Arrange -----
            val edition = edition(
                isbn10 = null,
                isbn13 = null,
            )

            // ----- Act -----
            val result = edition.toExternalLinkPills()

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `is null for a null edition`() {
            // ----- Arrange -----
            val edition: BookEdition? = null

            // ----- Act -----
            val result = edition.toExternalLinkPills()

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
