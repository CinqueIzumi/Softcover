package nl.rhaydus.softcover.core.component.bookcard

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookCardKeyIdTest {
    @Nested
    inner class LazyKeyTest {
        @Test
        fun `book, edition and series keys with the same id produce distinct lazy keys`() {
            // ----- Arrange -----
            val keys = listOf(
                BookCardKey.Book(bookId = 12),
                BookCardKey.Edition(editionId = 12),
                BookCardKey.Series(seriesId = 12),
            )

            // ----- Act -----
            val result = keys.map { it.lazyKey }

            // ----- Assert -----
            result.toSet() shouldHaveSize 3
        }

        @Test
        fun `equal keys produce equal lazy keys`() {
            // ----- Arrange -----
            val first = BookCardKey.Book(bookId = 12)
            val second = BookCardKey.Book(bookId = 12)

            // ----- Act -----
            val result = first.lazyKey

            // ----- Assert -----
            result shouldBe second.lazyKey
        }
    }
}
