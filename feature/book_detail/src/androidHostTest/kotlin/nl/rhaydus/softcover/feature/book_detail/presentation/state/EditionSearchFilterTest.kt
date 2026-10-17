package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import kotlinx.collections.immutable.persistentListOf
import nl.rhaydus.softcover.feature.book_detail.presentation.buildEdition
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class EditionSearchFilterTest {
    private val first = buildEdition(id = 1).copy(
        isbn10 = "0765326353",
        isbn13 = "9780765326355",
        publisher = "Tor Books",
    )
    private val second = buildEdition(id = 2).copy(
        isbn10 = null,
        isbn13 = "9781473222687",
        publisher = "Gollancz",
    )
    private val editions = persistentListOf(first, second)

    @Nested
    inner class FilterByEditionSearch {
        @Test
        fun `blank query returns every edition`() {
            // ----- Arrange -----
            val query = "   "

            // ----- Act -----
            val result = editions.filterByEditionSearch(query = query)

            // ----- Assert -----
            result shouldBe editions
        }

        @Test
        fun `matches an ISBN-13`() {
            // ----- Arrange -----
            val query = "9781473"

            // ----- Act -----
            val result = editions.filterByEditionSearch(query = query)

            // ----- Assert -----
            result shouldBe listOf(second)
        }

        @Test
        fun `matches an ISBN-10`() {
            // ----- Arrange -----
            val query = "0765326353"

            // ----- Act -----
            val result = editions.filterByEditionSearch(query = query)

            // ----- Assert -----
            result shouldBe listOf(first)
        }

        @Test
        fun `matches a publisher ignoring case and surrounding whitespace`() {
            // ----- Arrange -----
            val query = "  gollancz "

            // ----- Act -----
            val result = editions.filterByEditionSearch(query = query)

            // ----- Assert -----
            result shouldBe listOf(second)
        }

        @Test
        fun `returns nothing when no field matches`() {
            // ----- Arrange -----
            val query = "nonexistent"

            // ----- Act -----
            val result = editions.filterByEditionSearch(query = query)

            // ----- Assert -----
            result.shouldBeEmpty()
        }

        @Test
        fun `matches a hyphenated ISBN query against an unhyphenated ISBN-13`() {
            // ----- Arrange -----
            val target = buildEdition(id = 3).copy(isbn13 = "9780374710781")
            val query = "978-0-374-71078-1"

            // ----- Act -----
            val result = (editions + target).filterByEditionSearch(query = query)

            // ----- Assert -----
            result shouldBe listOf(target)
        }

        @Test
        fun `matches the publisher against the raw query without stripping hyphens`() {
            // ----- Arrange -----
            val hyphenated = buildEdition(id = 3).copy(publisher = "Farrar-Straus")
            val query = " Farrar-Straus "

            // ----- Act -----
            val result = (editions + hyphenated).filterByEditionSearch(query = query)

            // ----- Assert -----
            result shouldBe listOf(hyphenated)
        }
    }
}
