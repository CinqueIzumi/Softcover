package nl.rhaydus.softcover.feature.book_detail.presentation.mapper

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import nl.rhaydus.softcover.core.domain.model.Author
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ExternalLinksMapperTest {
    private fun edition(
        isbn10: String? = null,
        isbn13: String? = null,
        title: String? = null,
        authors: List<Author> = emptyList(),
    ): BookEdition = buildEdition(
        id = 1,
        authors = authors,
    ).copy(
        isbn10 = isbn10,
        isbn13 = isbn13,
        title = title,
    )

    @Nested
    inner class SearchIsbn {
        @Test
        fun `prefers the ISBN13`() {
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
        fun `falls back to the ISBN10 when there is no ISBN13`() {
            // ----- Arrange -----
            val edition = edition(isbn10 = "0306406152")

            // ----- Act -----
            val result = edition.searchIsbn

            // ----- Assert -----
            result shouldBe "0306406152"
        }

        @Test
        fun `falls back to the ISBN10 when the ISBN13 is blank`() {
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
            val edition = edition()

            // ----- Act -----
            val result = edition.searchIsbn

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class KoboQuery {
        @Test
        fun `joins the title and the first author`() {
            // ----- Arrange -----
            val edition = edition(
                title = "Dune",
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Frank Herbert",
                    ),
                    Author(
                        id = 2,
                        name = "Brian Herbert",
                    ),
                ),
            )

            // ----- Act -----
            val result = edition.koboQuery

            // ----- Assert -----
            result shouldBe "Dune Frank Herbert"
        }

        @Test
        fun `is only the title when there are no authors`() {
            // ----- Arrange -----
            val edition = edition(title = "Dune")

            // ----- Act -----
            val result = edition.koboQuery

            // ----- Assert -----
            result shouldBe "Dune"
        }

        @Test
        fun `is only the title when the first author name is blank`() {
            // ----- Arrange -----
            val edition = edition(
                title = "Dune",
                authors = listOf(
                    Author(
                        id = 1,
                        name = "   ",
                    ),
                ),
            )

            // ----- Act -----
            val result = edition.koboQuery

            // ----- Assert -----
            result shouldBe "Dune"
        }

        @Test
        fun `is null when the title is blank`() {
            // ----- Arrange -----
            val edition = edition(
                title = "   ",
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Frank Herbert",
                    ),
                ),
            )

            // ----- Act -----
            val result = edition.koboQuery

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `is null when the title is missing`() {
            // ----- Arrange -----
            val edition = edition(
                authors = listOf(
                    Author(
                        id = 1,
                        name = "Frank Herbert",
                    ),
                ),
            )

            // ----- Act -----
            val result = edition.koboQuery

            // ----- Assert -----
            result.shouldBeNull()
        }
    }

    @Nested
    inner class KoboSearchUrl {
        @Test
        fun `encodes spaces as percent twenty`() {
            // ----- Arrange -----
            val query = "Dune Frank Herbert"

            // ----- Act -----
            val result = koboSearchUrl(query = query)

            // ----- Assert -----
            result shouldBe "https://www.kobo.com/search?query=Dune%20Frank%20Herbert"
        }

        @Test
        fun `encodes reserved characters so they cannot split the query`() {
            // ----- Arrange -----
            val query = "Salt & Pepper #1"

            // ----- Act -----
            val result = koboSearchUrl(query = query)

            // ----- Assert -----
            result shouldBe "https://www.kobo.com/search?query=Salt%20%26%20Pepper%20%231"
        }

        @Test
        fun `encodes non-ASCII characters as UTF-8 bytes`() {
            // ----- Arrange -----
            val query = "Brontë"

            // ----- Act -----
            val result = koboSearchUrl(query = query)

            // ----- Assert -----
            result shouldBe "https://www.kobo.com/search?query=Bront%C3%AB"
        }

        @Test
        fun `keeps unreserved characters as they are`() {
            // ----- Arrange -----
            val query = "a-b_c.d~E9"

            // ----- Act -----
            val result = koboSearchUrl(query = query)

            // ----- Assert -----
            result shouldBe "https://www.kobo.com/search?query=a-b_c.d~E9"
        }
    }

    @Nested
    inner class ToExternalLinkPills {
        @Test
        fun `builds bookshop, amazon and open-library when only an ISBN is known`() {
            // ----- Arrange -----
            val hasIsbn = true
            val hasKoboQuery = false

            // ----- Act -----
            val result = toExternalLinkPills(
                hasIsbn = hasIsbn,
                hasKoboQuery = hasKoboQuery,
            )

            // ----- Assert -----
            result.shouldNotBeNull().map { it.key } shouldBe listOf(BOOKSHOP_KEY, AMAZON_KEY, OPEN_LIBRARY_KEY)
        }

        @Test
        fun `builds only kobo when only a Kobo query is known`() {
            // ----- Arrange -----
            val hasIsbn = false
            val hasKoboQuery = true

            // ----- Act -----
            val result = toExternalLinkPills(
                hasIsbn = hasIsbn,
                hasKoboQuery = hasKoboQuery,
            )

            // ----- Assert -----
            result.shouldNotBeNull().map { it.key } shouldBe listOf(KOBO_KEY)
        }

        @Test
        fun `orders kobo between amazon and open-library when both are known`() {
            // ----- Arrange -----
            val hasIsbn = true
            val hasKoboQuery = true

            // ----- Act -----
            val result = toExternalLinkPills(
                hasIsbn = hasIsbn,
                hasKoboQuery = hasKoboQuery,
            )

            // ----- Assert -----
            result.shouldNotBeNull().map { it.key } shouldBe
                listOf(BOOKSHOP_KEY, AMAZON_KEY, KOBO_KEY, OPEN_LIBRARY_KEY)
        }

        @Test
        fun `is null when neither an ISBN nor a Kobo query is known`() {
            // ----- Arrange -----
            val hasIsbn = false
            val hasKoboQuery = false

            // ----- Act -----
            val result = toExternalLinkPills(
                hasIsbn = hasIsbn,
                hasKoboQuery = hasKoboQuery,
            )

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
