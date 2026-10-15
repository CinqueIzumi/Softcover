package nl.rhaydus.softcover.feature.book_detail.presentation.state

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class IsbnPrefillTest {
    @Nested
    inner class IsbnPrefillFor {
        @Test
        fun `strips hyphens and spaces from an ISBN`() {
            // ----- Arrange -----
            val query = "978-0 374-71078 1"

            // ----- Act -----
            val result = isbnPrefillFor(query = query)

            // ----- Assert -----
            result shouldBe "9780374710781"
        }

        @Test
        fun `uppercases a trailing lowercase x check digit`() {
            // ----- Arrange -----
            val query = "0-8044-2957-x"

            // ----- Act -----
            val result = isbnPrefillFor(query = query)

            // ----- Assert -----
            result shouldBe "080442957X"
        }

        @Test
        fun `returns null for publisher text`() {
            // ----- Arrange -----
            val query = "Penguin"

            // ----- Act -----
            val result = isbnPrefillFor(query = query)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `returns null for a blank query`() {
            // ----- Arrange -----
            val query = "  - "

            // ----- Act -----
            val result = isbnPrefillFor(query = query)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
