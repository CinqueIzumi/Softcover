package nl.rhaydus.softcover.core.book.domain.usecase

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import nl.rhaydus.softcover.core.domain.model.Book
import nl.rhaydus.softcover.core.domain.model.BookEdition
import nl.rhaydus.softcover.core.domain.model.UserBook
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StatusChangeEditionTest {
    private fun shelvedBook(storedEditionId: Int?): Book {
        val userBook = mockk<UserBook>()

        every {
            userBook.editionId
        } returns storedEditionId

        val book = mockk<Book>()

        every {
            book.userBook
        } returns userBook

        return book
    }

    private fun offShelfBook(currentEditionId: Int?): Book {
        val book = mockk<Book>()

        every {
            book.userBook
        } returns null

        every {
            book.currentEdition
        } returns currentEditionId?.let { id ->
            mockk<BookEdition> {
                every {
                    this@mockk.id
                } returns id
            }
        }

        return book
    }

    @Nested
    inner class StatusChangeEditionId {
        @Test
        fun `shelved book returns its stored edition and ignores the requested one`() {
            // ----- Arrange -----
            val book = shelvedBook(storedEditionId = 5)

            // ----- Act -----
            val result = book.statusChangeEditionId(requestedEditionId = 9)

            // ----- Assert -----
            result shouldBe 5
        }

        @Test
        fun `shelved book with no stored edition returns null even when one is requested`() {
            // ----- Arrange -----
            val book = shelvedBook(storedEditionId = null)

            // ----- Act -----
            val result = book.statusChangeEditionId(requestedEditionId = 9)

            // ----- Assert -----
            result.shouldBeNull()
        }

        @Test
        fun `off-shelf book prefers the requested edition over its current edition`() {
            // ----- Arrange -----
            val book = offShelfBook(currentEditionId = 3)

            // ----- Act -----
            val result = book.statusChangeEditionId(requestedEditionId = 9)

            // ----- Assert -----
            result shouldBe 9
        }

        @Test
        fun `off-shelf book without a request falls back to its current edition`() {
            // ----- Arrange -----
            val book = offShelfBook(currentEditionId = 3)

            // ----- Act -----
            val result = book.statusChangeEditionId(requestedEditionId = null)

            // ----- Assert -----
            result shouldBe 3
        }

        @Test
        fun `off-shelf book without a request or a current edition returns null`() {
            // ----- Arrange -----
            val book = offShelfBook(currentEditionId = null)

            // ----- Act -----
            val result = book.statusChangeEditionId(requestedEditionId = null)

            // ----- Assert -----
            result.shouldBeNull()
        }
    }
}
