package nl.rhaydus.softcover.core.uibinding.book

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.component.transition.BookTransitionElement
import nl.rhaydus.softcover.core.component.transition.bookTransitionKey
import nl.rhaydus.softcover.core.domain.model.Book
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class BookTransitionKeysTest {
    @Nested
    inner class BookTransitionKeyExtension {
        @Test
        fun `the key matches the one built from the book id`() {
            // ----- Arrange -----
            val book = mockk<Book>()
            every {
                book.id
            } returns 42

            // ----- Act -----
            val key = book.transitionKey(
                element = BookTransitionElement.Title,
                surface = "rail",
            )

            // ----- Assert -----
            key shouldBe bookTransitionKey(
                element = BookTransitionElement.Title,
                bookId = 42,
                surface = "rail",
            )
        }

        @Test
        fun `without a surface the key has no surface suffix`() {
            // ----- Arrange -----
            val book = mockk<Book>()
            every {
                book.id
            } returns 7

            // ----- Act -----
            val key = book.transitionKey(element = BookTransitionElement.Cover)

            // ----- Assert -----
            key.value shouldBe "book-${BookTransitionElement.Cover.keyPart}-book-7"
        }
    }
}
