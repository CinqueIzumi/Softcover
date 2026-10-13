package nl.rhaydus.softcover.core.component.transition

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookTransitionKeyBuilderTest {
    @Nested
    inner class BookTransitionKeyFunction {
        @Test
        fun `the three elements of one book get three distinct keys`() {
            // ----- Arrange -----
            val elements = BookTransitionElement.entries

            // ----- Act -----
            val keys = elements.map { element ->
                bookTransitionKey(
                    element = element,
                    bookId = 42,
                )
            }

            // ----- Assert -----
            keys.toSet().size shouldBe elements.size
        }

        @Test
        fun `the same element of the same book gets the same key every time`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Title

            // ----- Act -----
            val first = bookTransitionKey(
                element = element,
                bookId = 42,
            )
            val second = bookTransitionKey(
                element = element,
                bookId = 42,
            )

            // ----- Assert -----
            first shouldBe second
        }

        @Test
        fun `different books get different keys for the same element`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Rating

            // ----- Act -----
            val first = bookTransitionKey(
                element = element,
                bookId = 1,
            )
            val second = bookTransitionKey(
                element = element,
                bookId = 2,
            )

            // ----- Assert -----
            first shouldNotBe second
        }

        @Test
        fun `the key string carries the element part and the book id`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Cover

            // ----- Act -----
            val key = bookTransitionKey(
                element = element,
                bookId = 42,
            )

            // ----- Assert -----
            key.value shouldBe "book-${element.keyPart}-book-42"
        }

        @Test
        fun `a surface is appended to the key string`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Title

            // ----- Act -----
            val key = bookTransitionKey(
                element = element,
                bookId = 42,
                surface = "rail",
            )

            // ----- Assert -----
            key.value shouldBe "book-${element.keyPart}-book-42-rail"
        }

        @Test
        fun `different surfaces keep one book's key distinct`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Title

            // ----- Act -----
            val rail = bookTransitionKey(
                element = element,
                bookId = 42,
                surface = "rail",
            )
            val grid = bookTransitionKey(
                element = element,
                bookId = 42,
                surface = "grid",
            )

            // ----- Assert -----
            rail shouldNotBe grid
        }

        @Test
        fun `a null surface differs from a non-null surface`() {
            // ----- Arrange -----
            val element = BookTransitionElement.Rating

            // ----- Act -----
            val bare = bookTransitionKey(
                element = element,
                bookId = 42,
                surface = null,
            )
            val surfaced = bookTransitionKey(
                element = element,
                bookId = 42,
                surface = "rail",
            )

            // ----- Assert -----
            bare shouldNotBe surfaced
        }
    }
}
